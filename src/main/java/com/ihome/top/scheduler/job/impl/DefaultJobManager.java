/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.config.SchedulerConfig;
import com.ihome.top.scheduler.exception.JobKeyDuplicatedException;
import com.ihome.top.scheduler.exception.JobNotFoundException;
import com.ihome.top.scheduler.exception.SchedulerException;
import com.ihome.top.scheduler.internal.log.Logger;
import com.ihome.top.scheduler.job.JobCompletedCallback;
import com.ihome.top.scheduler.job.JobExecutedCallback;
import com.ihome.top.scheduler.job.JobFailedHandler;
import com.ihome.top.scheduler.job.JobProvider;
import com.ihome.top.scheduler.job.JobReloadHandler;
import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.JobTimeoutHandler;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.scheduler.job.internal.impl.DefaultJob;
import com.ihome.top.scheduler.job.internal.impl.DependencyJob;
import com.ihome.top.scheduler.plan.JobExecutePlan;
import com.ihome.top.scheduler.plan.internal.JobExecutePlanManager;
import com.ihome.top.scheduler.plan.internal.impl.DefaultJobExecutePlanManager;
import com.ihome.top.scheduler.strategy.JobCallbackConditionEnum;
import com.ihome.top.scheduler.strategy.JobReloadConditionEnum;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 系统默认的Master节点Job管理器
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 * 
 */
public class DefaultJobManager implements com.ihome.top.scheduler.job.internal.JobManager {

	private final static Log logger = LogFactory.getLog(DefaultJobManager.class);
	
	// 配置
	private SchedulerConfig config;									// 配置信息
	
	// 任务产生相关
	private List<JobProvider> jobProviderList; 						// 任务产生器列表

	// 任务调度相关
	private AtomicLong jobIdGenerator = new AtomicLong(0); 			// job自增id
	private ConcurrentHashMap<Long, Job> doingJobMap; 				// 分发到Slave的Job
	private ConcurrentHashMap<String, BlockingQueue<Job>> group2undoJobQueueMap; 	// 处于就绪态的Job
	private BlockingQueue<Job> waitingJobQueue; 									// 处于等待中的Job
	private ConcurrentHashMap<JobExecutePlan, JobContainer> plan2JobContainerMap;	// 执行计划到多个JobKey的映射
	
	// 任务reload相关
	private volatile boolean isReloadJob; 				// Master端任务reload开关
	private List<JobReloadHandler> jobReloadHandlerList;// 
		
	// 任务回调相关
	private volatile boolean isJobCallback; 						// Master端任务回调开关
	private List<JobCompletedCallback> jobCompletedCallbackList; 	// 任务完成回调
	private List<JobExecutedCallback> jobExecutedCallbackList; 		// 任务执行一次完成回调
	private List<JobFailedHandler> jobFailedHandlerList; 			// 任务执行失败处理器链表
	private List<JobTimeoutHandler> jobTimeoutHandlerList; 			// 任务超时处理器链表
	
	private BlockingQueue<Job> pendingJobQueue;						// 待后续处理的任务, 回调, reload
	private Thread handlePendingJobThread;							// 处理pending job的线程
	// 
	private ConcurrentHashMap<JobKey, Job> key2JobMap;				// 
	private JobExecutePlanManager jobExecutePlanManager; 			// 任务执行计划管理器
	
	// 任务执行日志
	private Logger jobLogger;										// 任务日志
	private volatile boolean isLogJobExecution;						// 
	
	// Master端任务支持

	public DefaultJobManager(SchedulerConfig config) {
		this.config = config;
		isReloadJob = config.isEnableJobReload();
		isJobCallback = config.isEnableJobCallback();
		isLogJobExecution = config.isEnableLogJobExecution();
		
		jobProviderList = new LinkedList<JobProvider>();
		jobReloadHandlerList = new LinkedList<JobReloadHandler>();
		doingJobMap = new ConcurrentHashMap<Long, Job>();
		group2undoJobQueueMap = new ConcurrentHashMap<String, BlockingQueue<Job>>();
		waitingJobQueue = new LinkedBlockingQueue<Job>();
		plan2JobContainerMap = new ConcurrentHashMap<JobExecutePlan, JobContainer>();
		
		jobCompletedCallbackList = new LinkedList<JobCompletedCallback>();
		jobExecutedCallbackList = new LinkedList<JobExecutedCallback>();
		jobFailedHandlerList = new LinkedList<JobFailedHandler>();
		jobTimeoutHandlerList = new LinkedList<JobTimeoutHandler>();
		pendingJobQueue = new LinkedBlockingQueue<Job>();
		
		key2JobMap = new ConcurrentHashMap<JobKey, Job>();
	}

	@Override
	public boolean init() {
		logger.info("scheduler.JobManager: start to init");
		// 初始化日志系统
		enableJobExecutionLog(config.getJobLogDirectory(), config.getJobLogFileName());
		// 初始化jobExecutePlanManager
		jobExecutePlanManager = new DefaultJobExecutePlanManager();
		jobExecutePlanManager.registerPlanReadyObserver(this);
		if (!jobExecutePlanManager.init()) {
			logger.warn("Init jobExecutePlanManager failed.");
			return false;
		}
		// 任务Reload
		if(isReloadJob) {
			_enable_job_reload_();
		}
		// 任务回调
		if(isJobCallback) {
			_enable_job_callback_();
		}
		logger.info("scheduler.JobManager: init ok");
		return true;
	}

	@Override
	public boolean restart() {
		return stop() && init() && start();
	}

	@Override
	public boolean start() {
		async_load_job_();
		return jobExecutePlanManager.start();
	}

	@Override
	public boolean stop() {
		boolean ret = true;
		_close_job_reload_();
		ret = jobExecutePlanManager.stop();
		_close_job_callback_();
		closeJobExecutionLog();
		return ret;
	}

	@Override
	public void addJobProvider(JobProvider jobProvider) {
		jobProviderList.add(jobProvider);
	}

	@Override
	public void addAllJobProvider(Collection<JobProvider> jobProviderCollection) {
		jobProviderList.addAll(jobProviderCollection);
	}
	
	@Override
	public void addAllJobReloadHandler(Collection<JobReloadHandler> handlerCollection) {
		jobReloadHandlerList.addAll(handlerCollection);
	}

	@Override
	public void addJobReloadHandler(JobReloadHandler handler) {
		jobReloadHandlerList.add(handler);
	}
	
	@Override
	public void addJobExecutedCallback(JobExecutedCallback callback) {
		jobExecutedCallbackList.add(callback);
	}

	@Override
	public void addAllJobExecutedCallback(Collection<JobExecutedCallback> callbackCollection) {
		jobExecutedCallbackList.addAll(callbackCollection);
	}

	@Override
	public void addJobCompletedCallback(JobCompletedCallback callback) {
		jobCompletedCallbackList.add(callback);
	}

	@Override
	public void addAllJobCompletedCallback(
			Collection<JobCompletedCallback> callbackCollection) {
		jobCompletedCallbackList.addAll(callbackCollection);
	}

	@Override
	public void addJobFailedHandler(JobFailedHandler handler) {
		jobFailedHandlerList.add(handler);
	}

	@Override
	public void addAllJobFailedHandler(
			Collection<JobFailedHandler> handlerCollection) {
		jobFailedHandlerList.addAll(handlerCollection);
	}

	@Override
	public void addJobTimeoutHandler(JobTimeoutHandler handler) {
		jobTimeoutHandlerList.add(handler);
	}

	@Override
	public void addAllJobTimeoutHandler(
			Collection<JobTimeoutHandler> handlerCollection) {
		jobTimeoutHandlerList.addAll(handlerCollection);
	}

	@Override
	public void closeJobReload() {
		isReloadJob = false;
		_close_job_reload_();
	}

	@Override
	public void enableJobReload() {
		if(!isReloadJob) {
			isReloadJob = true;
			_enable_job_reload_();
		}
	}

	@Override
	public void reloadJob() {
		logger.warn("Try to reload all job");
		// 停止所有任务计划
		logger.warn("Try to remove all plan in jobExecutePlanManager");
		jobExecutePlanManager.removeAllPlan();
		// 清除执行计划与任务容器的映射
		plan2JobContainerMap.clear();
		// 清除就绪态的任务
		logger.warn("Try to clear all ready job");
		group2undoJobQueueMap.clear();
		// 清除等待的任务
		logger.warn("Try to clear all waiting job");
		waitingJobQueue.clear();
		
		logger.warn("Try marke all job removed");
		Job job = null;
		Iterator<Job> iterator = key2JobMap.values().iterator();
		while(iterator.hasNext()) {
			job = iterator.next();
			if(job != null) {
				job.markRemoved();
			}
		}
		// 清除所有任务
		key2JobMap.clear();
		
		// reload
		logger.warn("Try to start one thread load all job");
		async_load_job_();
	}

	@Override
	public void closeJobCallback() {
		isJobCallback = false;
		_close_job_callback_();
	}

	@Override
	public void enableJobCallback() {
		if(!isJobCallback) {
			isJobCallback = true;
			_enable_job_reload_();
		}
	}

	@Override
	public void closeJobExecutionLog() {
		isLogJobExecution = false;
		if(jobLogger != null) {
			jobLogger.shutdown();
			jobLogger = null;
		}
	}

	@Override
	public void enableJobExecutionLog(String logDirectory, String fileName) {
		if(jobLogger == null) {
			jobLogger = new Logger(logDirectory, fileName, config.getJobLogQueueSize(), config.getJobLogQueueThreshold());
			jobLogger.init();
		}
		isLogJobExecution = true;
	}
	
	@Override
	public Job newJob(JobKey key) throws JobKeyDuplicatedException {
		if(key == null || key.getGroup() == null || key.getName() == null) {
			throw new IllegalArgumentException("Job key(key.group, key.name) must not be null");
		}
		
		_exception_when_exists_(key);
		Job job = new DefaultJob(generateJobId(), key);
		job.setJobContext(new DefaultJobExecutionContext());
		return job;
	}

	@Override
	public Job newDependencyJob(JobKey key) throws JobKeyDuplicatedException {
		if(key == null || key.getGroup() == null || key.getName() == null) {
			throw new IllegalArgumentException("Job key(key.group, key.name) must not be null");
		}
		_exception_when_exists_(key);
		if(key2JobMap.containsKey(key)) {
			throw new JobKeyDuplicatedException(new StringBuilder("JobKey:").append(key).append(" is duplicated.").toString());
		}
		Job job = new DependencyJob(generateJobId(), key);
		job.setJobContext(new DefaultJobExecutionContext());
		return job;
	}
	
	private void _exception_when_exists_(JobKey key) throws JobKeyDuplicatedException {
		Job oldJob = key2JobMap.get(key);
		if(oldJob != null && !oldJob.isRemoved()) {
			throw new JobKeyDuplicatedException(new StringBuilder("JobKey:").append(key).append(" is duplicated.").toString());
		}
	}

	private Long generateJobId() {
		return jobIdGenerator.getAndIncrement();
	}

	// Master启动加载任务
	private void async_load_job_() {
		// 开一个线程异步的去加载
		new Thread(new Runnable() {
			public void run() {
				_load_job_();
			}
		}, "Top-Task-Scheduler-Job-Load-Thread").start();
	}
	
	private void _load_job_() {
		int total = 0;
		clear();
		for (JobProvider jobProvider : jobProviderList) {
			List<com.ihome.top.scheduler.job.Job> jobList = jobProvider.generate(this);
			for (com.ihome.top.scheduler.job.Job job : jobList) {
				try {
					addJob(job);
					total++;
				} catch (SchedulerException e) {
					e.printStackTrace();
					logger.error("Load one wrong job", e);
				}
			}
		}
		logger.info(new StringBuilder("scheduler.JobManager: loaded ").append(total).append(" jobs"));
	}

	// 清除任务
	private void clear() {
		doingJobMap.clear();
		Iterator<String> iterator = group2undoJobQueueMap.keySet().iterator();
		String group = null;
		BlockingQueue<Job> queue = null;
		while (iterator.hasNext()) {
			group = iterator.next();
			queue = group2undoJobQueueMap.get(group);
			if (queue != null) {
				queue.clear();
			}
		}
		group2undoJobQueueMap.clear();
		plan2JobContainerMap.clear();
		waitingJobQueue.clear();
	}

	@Override
	public void addJob(com.ihome.top.scheduler.job.Job job) throws SchedulerException {
		JobExecutePlan plan = job.getExecutePlan();
		((Job) job).setJobContext(new DefaultJobExecutionContext());
		addIfNeedGroup(job.getKey().getGroup());
		//logger.info(new StringBuilder("Plan:").append(plan).append(", hashCode:").append(plan.hashCode()));
		JobContainer oldJobContainer = null;
		JobContainer jobContainer = new JobContainer(new LinkedList<Job>());
		oldJobContainer = plan2JobContainerMap.putIfAbsent(plan, jobContainer);
		if(null == oldJobContainer) {
			// Add new plan
			logger.info(new StringBuilder("Add new plan:").append(plan));
			// 这里为什么要加个锁, 是为了保证plan被创建好, 防止多线程出问题
			try {
				jobContainer._lock_.writeLock().lock();
				jobExecutePlanManager.addPlan(plan);
			}  finally{
				jobContainer._lock_.writeLock().unlock();
			}
		} else {
			jobContainer = oldJobContainer;
		}
		
		try {
			jobContainer._lock_.writeLock().lock();
			jobContainer.jobList.add((Job)job);
		} finally{
			jobContainer._lock_.writeLock().unlock();
		}
		
		// 设置任务下次执行的时间
		((Job) job).setNextExecuteTime(jobExecutePlanManager.nextExecuteTime(plan));
		key2JobMap.put(job.getKey(), (Job)job);
		this.waitingJobQueue.add((Job)job);
		// 任务处于等待状态
		((Job) job).setStatus(JobStatusEnum.JOB_STATUS_WAITING);
	}

	@Override
	public Job executed(JobExecutionInfo jobExecutionInfo) {
		Long jobId = jobExecutionInfo.getJobId();
		Job job = doingJobMap.remove(jobId);
		if (job != null) {
			//job.setStatus(JobStatusEnum.JOB_STATUS_EXECUTED);
			job.executed(jobExecutionInfo);
			try {
				pendingJobQueue.put(job);
			} catch (InterruptedException e) {
				logger.error("OOPS：Exception：", e);
				Thread.currentThread().interrupt();
			}
			// 任务执行日志
			if(isLogJobExecution) {
				jobLogger.log((job.toString() + "\n").getBytes());
			}
			return job;
		} else {
			logger.error(new StringBuilder("Can not find doing job for id: ").append(jobId));
			return null;
		}
	}
	
	// 任务被执行了
	private void _executed_(Job job) throws InterruptedException {
		
		if(job.isRemoved()) {
			// 任务被标记为删除
			return;
		}
		
		job.setStatus(JobStatusEnum.JOB_STATUS_EXECUTED);
		if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_EXECUTED) || job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_EXECUTED)) {
			_stop_schedule_(job);
			_remove_(job);
			
			if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_EXECUTED)) {
				_reload_(job);
			} 
			if(job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_EXECUTED)) {
				_callback_(job);
			}
		} else {
			// 默认继续执行
			job.setNextExecuteTime(jobExecutePlanManager.nextExecuteTime(job.getExecutePlan()));
			job.setStatus(JobStatusEnum.JOB_STATUS_WAITING);
			waitingJobQueue.put(job);
		}
	}
	
	// 任务超时了
	private void _timeout_(Job job) throws InterruptedException {
		
		if(job.isRemoved()) {
			// 任务被标记为删除
			return;
		}
		
		job.setStatus(JobStatusEnum.JOB_STATUS_TIMEOUT);
		
		if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_TIMEOUT) || job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_TIMEOUT)) {
			logger.warn(new StringBuilder("The job:").append(job.getKey()).append(" timeout, so stop schedule this job and remove it, then try reload and callback"));
			_stop_schedule_(job);
			_remove_(job);
			
			if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_TIMEOUT)) {
				_reload_(job);
			} 
			if(job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_TIMEOUT)) {
				_callback_(job);
			}
		} else {
			// 默认继续等待
			job.setStatus(JobStatusEnum.JOB_STATUS_WAITING);
			waitingJobQueue.put(job);
		}
	}
	
	// 任务执行失败了
	private void _failed_(Job job) throws InterruptedException {
		job.setStatus(JobStatusEnum.JOB_STATUS_FAILED);
		// 停止调度, 移除任务
		_stop_schedule_(job);
		_remove_(job);
		logger.warn(new StringBuilder("Execute job:").append(job.getKey()).append(" failed, so stop schedule this job and remove it, then try reload and callback"));
		if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_FAILED)) {
			_reload_(job);
		} else {
			logger.warn(String.format("Not reload for job key:%s, reloadStrategry:%s , for FAILED", job.getKey(), job.getReloadStrategry()));
		}
		if (job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_FAILED)) {
			_callback_(job);
		} else {
			logger.warn(String.format("Not callback for job key:%s, callbackStrategry:%s , for FAILED", job.getKey(), job.getCallbackStrategry()));
		}
	}
	
	// 任务完成了
	private void _completed_(Job job) throws InterruptedException {
		job.setStatus(JobStatusEnum.JOB_STATUS_COMPLETED);
		// 停止调度, 移除任务
		_stop_schedule_(job);
		_remove_(job);
		logger.info(new StringBuilder("Execute job:").append(job.getKey()).append(" completed, so stop schedule this job and remove it, then try reload and callback"));
		if(job.isReload(JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_COMPLETED)) {
			_reload_(job);
		} 
		if (job.isCallback(JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_COMPLETED)) {
			_callback_(job);
		}
	}
	
	private void _reload_(Job job) {
		// 先reload
		for(JobReloadHandler handler : jobReloadHandlerList) {
			try {
				Job newJob = (Job)handler.reload(this, job);
				if(newJob != null) {
					//logger.info(new StringBuilder("Re schedule one reloaded job:").append(newJob.getKey()));
					addJob(newJob);
				}
			} catch (SchedulerException e){
				logger.error("OOPS：Exception：", e);
			}
		}
	}
	
	private void _callback_(Job job) {
		// 后回调
		if(job.getCallbackCondition() == JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_EXECUTED) {
			for(JobExecutedCallback callback : jobExecutedCallbackList) {
				callback.callback(this, job);
			}
		} else if(job.getCallbackCondition() == JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_COMPLETED) {
			for(JobCompletedCallback callback : jobCompletedCallbackList) {
				callback.callback(this, job);
			}
		} else if(job.getCallbackCondition() == JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_TIMEOUT) {
			for(JobTimeoutHandler handler : jobTimeoutHandlerList) {
				handler.handle(this, job);
			}
		}  else if(job.getCallbackCondition() == JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_FAILED) {
			for(JobFailedHandler handler : jobFailedHandlerList) {
				handler.handle(this, job);
			}
		}
	}

	private void _stop_schedule_(Job job) {
		JobExecutePlan plan = job.getExecutePlan();
		// 请求写锁
		JobContainer jobContainer = plan2JobContainerMap.get(plan);
		if(jobContainer != null) {
			try {
				jobContainer._lock_.writeLock().lock();
				jobContainer.jobList.remove(job);
				if(jobContainer.jobList.isEmpty()) {
					plan2JobContainerMap.remove(plan);
					this.jobExecutePlanManager.removePlan(plan);
				}
			} finally {
				jobContainer._lock_.writeLock().unlock();
			}
		} else {
			logger.info(String.format("Error the job: %s, not in schedule plan", job.getKey()));
		}
	}
	
	private void _remove_(Job job) {
		key2JobMap.remove(job.getKey());
	}

	@Override
	public Collection<Job> dispatch(List<PullJobRequest> requestList,
			long maxWaitingTime, SlaveWorker worker) {
		logger.info("jobManager try to dispatch job");
		Collection<Job> collection = new LinkedList<Job>();
		long _start = System.currentTimeMillis();
		long _time = maxWaitingTime;
		int number = 0;
		for (PullJobRequest request : requestList) {
			BlockingQueue<Job> queue = group2undoJobQueueMap.get(request.getGroup());
			if (queue != null) {
				number = request.getMax();
				while (number > 0) {
					Job job = null;
					try {
						job = queue.poll(_time, TimeUnit.MILLISECONDS);
						_time -= (System.currentTimeMillis() - _start);
						if (maxWaitingTime < _time) {
							// time out
							break;
						}
						
						if (job == null) {
							break;
						}
							
						if (!job.isDependencySatisfied()) {
							// logger.warn(new
							// StringBuilder("Job's dependency not satisfied, jobId:").append(job.getId()).toString());
							queue.put(job);
							continue;
						}
					} catch (InterruptedException e) {
						logger.error("dispath job error while waiting", e);
						Thread.interrupted();
					}
					job.dispatch(worker);
					collection.add(job);
					this.doingJobMap.put(job.getId(), job);
					number--;
				}
			}
		}
		logger.info(new StringBuilder("jobManager dispatch ").append(collection.size()).append(" job"));
		return collection;
	}
	
	@Override
	public void releaseDoingJob(SlaveWorker worker) {
		logger.warn(String.format("jobManager try release job hold by slave: %s", worker));
		Job job = null;
		Long id = null;
		Iterator<Long> iterator = doingJobMap.keySet().iterator();
		while(iterator.hasNext()) {
			id = iterator.next();
			job = doingJobMap.get(id);
			if(job != null) {
				if(worker.equals(job.getWorker())) {
					doingJobMap.remove(id);
					job.setStatus(JobStatusEnum.JOB_STATUS_FAILED);
					logger.warn(String.format("Release job:%s, hold by slave:%s", job.getKey(), worker));
					try {
						pendingJobQueue.put(job);
					} catch (InterruptedException e) {
						//
						logger.error(e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	@Override
	public Collection<Job> dispatch(List<PullJobRequest> requestList,
			SlaveWorker worker) {
		return dispatch(requestList, Long.MAX_VALUE, worker);
	}

	@Override
	public int getDoingJobCount() {
		return doingJobMap.size();
	}

	@Override
	public int getDoneJobCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUndoJobCount() {
		// FIXME
		return 0;
	}

	@Override
	public Job removeJob(JobKey key) throws JobNotFoundException {
		Job job = key2JobMap.get(key);
		
		if (job == null) {
			throw new JobNotFoundException(new StringBuilder(
					"Not found job for jobkey:").append(key.toString())
					.toString());
		}
		
		job.markRemoved();
		return job;
	}

	@Override
	public void ready(JobExecutePlan plan) {
		try {
			Date now = new Date();
			int total = 0;
			BlockingQueue<Job> groupQueue = null;
			JobContainer jobContainer = this.plan2JobContainerMap.get(plan);
			if(jobContainer != null) {
				try {
					jobContainer._lock_.readLock().lock();
					for(Job job : jobContainer.jobList) {
						// 将任务从等待队列移动到所属分组就绪态队列
						if(this.waitingJobQueue.remove(job)) {
							groupQueue = group2undoJobQueueMap.get(job.getKey().getGroup());
							// 这里是有可能为null, 如reload时
							if(groupQueue != null) {
								job.setStatus(JobStatusEnum.JOB_STATUS_READY);
								groupQueue.add(job);
								total++;
								//logger.info(String.format("Fired job, jobkey:%s", job.getKey()));
							} else {
								logger.warn(String.format("Not normal, groupQueue for group:%s is null", job.getKey().getGroup()));
							}
						} else {
							//logger.warn(new StringBuilder("Job is ready, but this job not in waitingJobQueue, jobkey:").append(job.getKey().toString()));
							// 判断任务是否超时
							if(job.isTimeout(now)) {
								logger.warn(String.format("Job: %s, status:%s is time out !!!", job.getKey().toString(), job.getStatus()));
								job.setStatus(JobStatusEnum.JOB_STATUS_TIMEOUT);
								pendingJobQueue.put(job);
							} else {
								logger.warn(String.format("Job: %s, status:%s is not time out, but not in waiting queue, so waiting it !!!", job.getKey().toString(), job.getStatus()));
							}
 						}
					}
					logger.info(String.format("fired %d jobs", total));
				} finally {
					jobContainer._lock_.readLock().unlock();
				}
			} else {
				logger.warn(String.format("Not normal, jobContainer for plan:%s is null", plan.toString()));
			}
		} catch (InterruptedException e) {
			logger.error("OOPS：Exception：", e);
			Thread.currentThread().interrupt();
		} finally {
			
		}
	}

	/**
	 * 启动任务reload线程
	 */
	private void _enable_job_reload_() {

		// 已经启动了
		if (handlePendingJobThread != null) {
			return;
		}
		_start_pending_handle_thread_();
	}

	/**
	 * 启动任务reload线程
	 */
	private void _enable_job_callback_() {
		// 已经启动了
		if (handlePendingJobThread != null) {
			return;
		}
		_start_pending_handle_thread_();
	}

	private void _start_pending_handle_thread_() {
		// callback 线程初始化
		handlePendingJobThread = new Thread(new HandlePendingJobTask(), "Top-Task-Job-Callback-Thread");
		handlePendingJobThread.setDaemon(true);
		handlePendingJobThread.start();
	}
	
	private void _stop_pending_handle_thread_() {
		if (handlePendingJobThread == null) {
			return;
		}
		if(!isReloadJob && !isJobCallback) {
			handlePendingJobThread.interrupt();
			handlePendingJobThread = null;
		}
	}
	/**
	 * 关闭任务reload线程
	 */
	private void _close_job_reload_() {
		isReloadJob = false;
		_stop_pending_handle_thread_();
	}
	
	/**
	 * 关闭任务reload线程
	 */
	private void _close_job_callback_() {
		isJobCallback = false;
		_stop_pending_handle_thread_();
	}

	// 
	private void addIfNeedGroup(String group) {
		group2undoJobQueueMap.putIfAbsent(group, new LinkedBlockingQueue<Job>());
	}
	
	// 后台回调线程
	private class HandlePendingJobTask implements Runnable {
		
		@Override
		public void run() {
			try {
				logger.info(new StringBuilder(Thread.currentThread().getName()).append(" started"));
				Job job = null;
				JobStatusEnum status = null;
				while(!Thread.currentThread().isInterrupted()) {
					job = pendingJobQueue.take();
					status = job.getStatus();
					if(status == JobStatusEnum.JOB_STATUS_TIMEOUT) {
						// 超时
						_timeout_(job);
					} else if(status == JobStatusEnum.JOB_STATUS_FAILED) {
						// 任务执行失败
						_failed_(job);
					} else if(status == JobStatusEnum.JOB_STATUS_EXECUTED){
						// 任务执行成功
						Date nextExecuteTime = jobExecutePlanManager.nextExecuteTime(job.getExecutePlan());
						if (nextExecuteTime == null || job.isRemoved()) {
							// 任务执行完成
							_completed_(job);
						} else {
							// 设置任务下次执行时间
							job.setNextExecuteTime(nextExecuteTime);
							_executed_(job);
						}
					}
				}
			} catch(InterruptedException e) {
				logger.error("OOPS：Exception：", e);
			}
			
			logger.info(new StringBuilder(Thread.currentThread().getName()).append(" stoped"));
		}
	}
	
	// 任务容器
	private class JobContainer {
		public List<Job> jobList;
		public ReadWriteLock _lock_;
		
		public JobContainer(List<Job> jobList) {
			this.jobList = jobList;
			_lock_ = new ReentrantReadWriteLock();
		}
	}
}
