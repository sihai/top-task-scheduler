/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.executor;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.exception.JobExecutionException;
import com.ihome.top.scheduler.job.AsyncJobConsumer;
import com.ihome.top.scheduler.job.JobCompletedReporter;
import com.ihome.top.scheduler.job.JobConsumer;
import com.ihome.top.scheduler.job.JobResult;
import com.ihome.top.scheduler.job.SyncJobConsumer;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.internal.Job;

/**
 * <p>
 * 抽象的任务组执行单元
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public abstract class AbstractJobGroupExecutor implements JobGroupExecutor {
	
	private static final Log logger = LogFactory.getLog(AbstractJobGroupExecutor.class);
	
	private String group;							// 任务分组名
	private ResourceConfig rconfig;					// 资源配置
	protected JobConsumer jobConsumer;				// 业务任务执行器
	private AtomicLong completed;					// 已经执行的任务数量
	// 下面是为了提供过载保护
	private AtomicBoolean jobAssignThreadStared;	// 任务下放线程启动了吗
	private Thread jobAssignThread;					// 任务下放，过载保护
	private BlockingQueue<BufferedJob> jobBuffer;	// 等待下放的任务缓冲
	private AtomicBoolean isBusy;					// 执行器是否方忙
	private ReentrantLock assignLock;				// 下放任务开始执行信号, 保护锁
	private Condition assignCondition;				// 下放任务开始执行信号
	private boolean isAssign;						// 下放任务是否执行

	public AbstractJobGroupExecutor(String group, ResourceConfig rconfig, JobConsumer jobConsumer) {
		this.group = group;
		this.rconfig = rconfig;
		this.jobConsumer = jobConsumer;
		this.completed = new AtomicLong(0L);
		this.jobAssignThreadStared = new AtomicBoolean(false);
		this.isBusy = new AtomicBoolean(false);
		this.assignLock = new ReentrantLock();
		this.assignCondition = this.assignLock.newCondition();
		this.isAssign = false;
		
		logger.warn(String.format("New one jobGroupExecutor for group:%s,rconfig:%s", group, rconfig.toString()));
	}
	
	@Override
	public String getGroup() {
		return group;
	}
	
	@Override
	public ResourceConfig getResourceConfig() {
		return rconfig;
	}

	@Override
	public JobConsumer getJobConsumer() {
		return jobConsumer;
	}
	
	@Override
	public long completed() {
		return completed.get();
	}
	
	/**
	 * 提交任务的模板方法, 提供通用的过载保护
	 */
	@Override
	public void doJob(Job job, JobCompletedReporter reporter) {
		before();
		try {
			execute(job, reporter);
		} catch(JobGroupExecutorBusyException e) {
			// 过载
			logger.warn(new StringBuilder("Job Executor Group(").append(group).append(") is busy."));
			// 进入buffer区, 唤醒后台
			bufferJob(job, reporter);
		}
		after();
	}
	
	/**
	 * 计算剩余计算能力模板方法, 考虑 isBusy
	 */
	@Override
	public final int remainingCapacity() {
		if(isBusy.get()) {
			return 0;
		}
		
		return availableCapacity();
	}
	
	@Override
	public final void shutdown() {
		// TODO
		shutdownNow();
	}
	
	/**
	 * 关闭模板方法, 因为父类有些资源也要释放
	 */
	@Override
	public final void shutdownNow() {
		this.jobAssignThreadStared.set(false);
		this.jobAssignThread.interrupt();
		this.jobBuffer.clear();
		this.isBusy.set(false);
		// 释放子类资源
		shutdownExecuteNow();
		// 释放jobConsumer的资源
		//jobConsumer.shutdownNow();
	}
	
	/**
	 * 留给子类实现, 真正的提交任务, 可能是将Job包装成一个Runnable提交到线程池执行, 等等
	 * @param job
	 * @param reporter
	 * @throws JobGroupExecutorBusyException
	 */
	protected abstract void execute(Job job, JobCompletedReporter reporter) throws JobGroupExecutorBusyException;
	
	/**
	 * 子类提供的剩余计算能力, 父类需要同时考虑isBusy和子类的availableCapacity
	 * @return
	 */
	protected abstract int availableCapacity();
	
	/**
	 * 子类提供的关闭, 父类也有些资源需要释放
	 */
	protected abstract void shutdownExecute();
	
	/**
	 * 子类提供的立即关闭, 父类也有些资源需要释放
	 */
	protected abstract void shutdownExecuteNow();
	
	/**
	 * 提交异步任务
	 * @param job
	 * @param reporter
	 */
	protected void submit(Job job, JobCompletedReporter reporter) {
		((AsyncJobConsumer)jobConsumer).work(job, reporter);
	}
	/**
	 * 事前处理
	 */
	private void before() {
		if(!jobAssignThreadStared.getAndSet(true)) {
			startJobAssignThread();
		}
	}
	
	/**
	 * 事后处理 
	 */
	private void after() {
		completed.incrementAndGet();
	}
	
	/**
	 * 
	 * @param job
	 * @param reporter
	 */
	private void bufferJob(Job job, JobCompletedReporter reporter) {
		
		try {
			isBusy.set(true);				// 标识任务组执行器繁忙
			assignLock.lock();
			assignCondition.signalAll();	// 唤醒后台超额任务提交线程
			jobBuffer.put(new BufferedJob(job, reporter));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e);
		} finally {
			assignLock.unlock();
		}
	}
	
	/**
	 * 启动后台超额任务提交线程, 这里是懒启动, 不会在JobGroupExecutor初始化时就启动, 而是在需要时才启动
	 */
	private void startJobAssignThread() {
		this.jobBuffer = new LinkedBlockingQueue<BufferedJob>();		// 先不限制大小，这个缓冲不会过大
		this.jobAssignThread = new Thread(new JobAssignThreadTask(), "Top-Task-Scheduler-Job-Consumer-Group(" + group + ")-Job-Assign-Thread");
		this.jobAssignThread.setDaemon(true);
		this.jobAssignThread.start();
	}
	
	/**
	 * 后台超额任务提交线程
	 * @author sihai
	 *
	 */
	private class JobAssignThreadTask implements Runnable {

		@Override
		public void run() {
			BufferedJob bufferedJob = null;
			while(!Thread.currentThread().isInterrupted()) {
				try {
					assignLock.lock();
					while(!isAssign) {
						// 等待信号
						assignCondition.await();
					}
					
					// 下放任务
					while(true) {
						bufferedJob = jobBuffer.poll();
						if(bufferedJob == null) {
							// 所有的超额任务都没提交了, 没事了
							break;
						}
						try {
							execute(bufferedJob.job, bufferedJob.reporter);
						} catch(JobGroupExecutorBusyException e) {
							logger.warn(new StringBuilder("Job Executor Group()").append(group).append(" still busy, try to sleep 1 second"));
							// 重试, 缓点
							Thread.sleep(1000);
						}
					}
					
					// buffer区清理干净了, 可以拉新任务了
					isBusy.set(false);
					logger.warn(new StringBuilder("Job Executor Group()").append(group).append(" released"));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.error(e);
				} finally {
					assignLock.unlock();
				}	
			}
		}
	}
	
	/**
	 * 包装一下, 处理起来方便些
	 * @author sihai
	 *
	 */
	private class BufferedJob {
		public Job job;
		public JobCompletedReporter reporter;
		
		public BufferedJob(Job job, JobCompletedReporter reporter) {
			this.job = job;
			this.reporter = reporter;
		}
	}
	
	/**
	 * 同步执行任务
	 * @author sihai
	 *
	 */
	public class SyncJobTask implements Runnable {
		
		private Job job;
		private JobCompletedReporter reporter;
		
		public SyncJobTask(Job job, JobCompletedReporter reporter) {
			this.job = job;
			this.reporter = reporter;
		}
		
		@Override
		public void run() {
			JobExecutionInfo jobExecutionInfo = job.getJobExecutionInfo();
			try {
				jobExecutionInfo.setSlaveJobStartTime(new Date());
				JobResult jobResult = ((SyncJobConsumer)jobConsumer).work(job);
				jobExecutionInfo.setSlaveJobEndTime(new Date());
				jobExecutionInfo.setIsSucceed(true);
				jobExecutionInfo.setJobResult(jobResult);
			} catch (Throwable t) {
				//
				jobExecutionInfo.setIsSucceed(false);
				jobExecutionInfo.setExecutionException(new JobExecutionException(t));
				logger.error("Execute job failed", t);
				t.printStackTrace();
			} finally {
				// report
				reporter.report(jobExecutionInfo);
			}
		}
	}
}
