/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.command.CommandConstants;
import com.ihome.top.scheduler.command.master.PullJobCommandHandler;
import com.ihome.top.scheduler.command.master.PushResultCommandHandler;
import com.ihome.top.scheduler.config.SchedulerConfig;
import com.ihome.top.scheduler.exception.JobKeyDuplicatedException;
import com.ihome.top.scheduler.exception.JobNotFoundException;
import com.ihome.top.scheduler.exception.SchedulerException;
import com.ihome.top.scheduler.job.JobCompletedCallback;
import com.ihome.top.scheduler.job.JobExecutedCallback;
import com.ihome.top.scheduler.job.JobFailedHandler;
import com.ihome.top.scheduler.job.JobProvider;
import com.ihome.top.scheduler.job.JobReloadHandler;
import com.ihome.top.scheduler.job.JobTimeoutHandler;
import com.ihome.top.scheduler.job.impl.DefaultJobManager;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.scheduler.job.internal.JobManager;
import com.ihome.top.waverider.MasterNode;
import com.ihome.top.waverider.Node;
import com.ihome.top.waverider.SlaveWorker;
import com.ihome.top.waverider.config.WaveriderConfig;
import com.ihome.top.waverider.master.DefaultMasterNode;
import com.ihome.top.waverider.master.SlaveListenter;

/**
 * <p>
 * 系统默认的Master节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultMasterScheduler implements com.ihome.top.scheduler.internal.MasterScheduler {

	private static final Log logger = LogFactory.getLog(DefaultMasterScheduler.class);
	
	private SchedulerConfig config;									// 节点配置信息
	private Node node;												// 网络通信节点
	private List<JobProvider> jobProviderList;						// 任务产生器列表
	private List<JobReloadHandler> jobReloadHandlerList;			// 任务产生器列表
	private JobManager jobManager;									// 任务管理器
	private List<JobExecutedCallback> jobExecutedCallbackList;  	// 任务完成回调
	private List<JobCompletedCallback> jobCompletedCallbackList;  	// 任务完成回调
	private List<JobFailedHandler> jobFailedHandlerList;  			// 任务执行失败处理器链表
	private List<JobTimeoutHandler> jobTimeoutHandlerList;  		// 任务超时处理器链表
	
	public DefaultMasterScheduler(SchedulerConfig config) {
		this.config = config;
		jobProviderList = new LinkedList<JobProvider>();
		jobReloadHandlerList = new LinkedList<JobReloadHandler>();
		jobExecutedCallbackList = new LinkedList<JobExecutedCallback>();
		jobCompletedCallbackList = new LinkedList<JobCompletedCallback>();
		jobFailedHandlerList = new LinkedList<JobFailedHandler>();
		jobTimeoutHandlerList = new LinkedList<JobTimeoutHandler>();
	}
	
	@Override
	public boolean init() {
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                 if (!(e instanceof ThreadDeath)) {
                	 logger.error("OOPS:", e);
                     System.err.print(String.format("Exception in thread: %s", t.getName()));
                     e.printStackTrace(System.err);
                 }   
            }
        });

		// 初始化jobManager
		jobManager = new DefaultJobManager(config);
		jobManager.addAllJobProvider(jobProviderList);
		jobManager.addAllJobReloadHandler(jobReloadHandlerList);
		jobManager.addAllJobExecutedCallback(jobExecutedCallbackList);
		jobManager.addAllJobCompletedCallback(jobCompletedCallbackList);
		jobManager.addAllJobFailedHandler(jobFailedHandlerList);
		jobManager.addAllJobTimeoutHandler(jobTimeoutHandlerList);
		
		if(!jobManager.init()) {
			logger.warn("Init jobManager failed.");
			return false;
		}
		
		// 初始化任务reload
		if(config.isEnableJobCallback()) {
			jobManager.enableJobCallback();
		}
		
		// 初始化任务reload
		if(config.isEnableJobReload()) {
			jobManager.enableJobReload();
		}
		
		// 初始化Master通信节点
		node = new DefaultMasterNode(_make_waverider_config_(config));
		// 添加Master端处理Slave拉job命令处理器
		node.addCommandHandler(CommandConstants.PULL_JOB_COMMAND, new PullJobCommandHandler(this));
		// 添加Master端处理Slave推送任务结果命令处理器
		node.addCommandHandler(CommandConstants.PUSH_RESULT_COMMAND, new PushResultCommandHandler(this));
		// 注册SlaveListener
		((MasterNode)node).registerSlaveListener(new SlaveListenterSupport());
		
		if(!node.init()) {
			logger.warn("Init MasterNode failed.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean start() {
		return jobManager.start() && node.start();
	}
	
	@Override
	public boolean stop() {
		return jobManager.stop() && node.stop();
	}
	
	@Override
	public boolean restart() {
		return jobManager.restart() && node.restart();
	}
	
	@Override
	public void addJobExecutedCallback(JobExecutedCallback callback) {
		jobExecutedCallbackList.add(callback);
	}
	
	@Override
	public void addJobCompletedCallback(JobCompletedCallback callback) {
		jobCompletedCallbackList.add(callback);
	}

	@Override
	public void addJobFailedHandler(JobFailedHandler handler) {
		jobFailedHandlerList.add(handler);
	}

	@Override
	public void addJobProvider(JobProvider jobProvider) {
		jobProviderList.add(jobProvider);
	}
	
	@Override
	public void addJobReloadHandler(JobReloadHandler handler) {
		jobReloadHandlerList.add(handler);
	}

	@Override
	public void addJobTimeoutHandler(JobTimeoutHandler handler) {
		jobTimeoutHandlerList.add(handler);
	}
	
	@Override
	public void closeJobCallback() {
		if(jobManager != null) {
			jobManager.closeJobCallback();
		}
		config.setEnableJobCallback(false);
	}

	@Override
	public void enableJobCallback() {
		if(jobManager != null) {
			jobManager.enableJobCallback();
		}
		config.setEnableJobCallback(true);
	}

	@Override
	public void closeJobReload() {
		if(jobManager != null) {
			jobManager.closeJobReload();
		}
		config.setEnableJobReload(false);
	}

	@Override
	public void enableJobReload() {
		if(jobManager != null) {
			jobManager.enableJobReload();
		}
		config.setEnableJobReload(true);
	}
	
	@Override
	public void reloadJob() {
		jobManager.reloadJob();
	}
	
	@Override
	public void closeJobExecutionLog() {
		if(jobManager != null) {
			jobManager.closeJobExecutionLog();
		}
		config.setEnableLogJobExecution(false);
	}

	@Override
	public void enableJobExecutionLog(String logDirectory, String fileName) {
		if(jobManager != null) {
			jobManager.enableJobExecutionLog(logDirectory, fileName);
		}
		config.setJobLogDirectory(logDirectory);
		config.setJobLogFileName(fileName);
		config.setEnableLogJobExecution(true);
	}
	
	@Override
	public Job completed(JobExecutionInfo jobExecutionInfo) {
		return jobManager.executed(jobExecutionInfo);
	}

	@Override
	public Collection<Job> dispatch(List<PullJobRequest> requestList, long maxWaitingTime, SlaveWorker worker) {
		return jobManager.dispatch(requestList, maxWaitingTime, worker);
	}

	@Override
	public Collection<Job> dispatch(List<PullJobRequest> requestList, SlaveWorker worker) {
		return jobManager.dispatch(requestList, worker);
	}
	
	/***************************************************************************
	 * 						Job Management
	 ***************************************************************************/
	@Override
	public void addJob(com.ihome.top.scheduler.job.Job job)
			throws SchedulerException {
		jobManager.addJob(job);
	}

	@Override
	public int getDoingJobCount() {
		return jobManager.getDoingJobCount();
	}
	
	@Override
	public int getDoneJobCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUndoJobCount() {
		return jobManager.getUndoJobCount();
	}

	@Override
	public com.ihome.top.scheduler.job.JobManager getJobManager() {
		return this.jobManager;
	}
	
	@Override
	public com.ihome.top.scheduler.job.Job newDependencyJob(JobKey key) throws JobKeyDuplicatedException {
		return jobManager.newDependencyJob(key);
	}

	@Override
	public com.ihome.top.scheduler.job.Job newJob(JobKey key) throws JobKeyDuplicatedException {
		return jobManager.newJob(key);
	}

	@Override
	public com.ihome.top.scheduler.job.Job removeJob(JobKey key)
			throws JobNotFoundException {
		return jobManager.removeJob(key);
	}
	
	private WaveriderConfig _make_waverider_config_(SchedulerConfig config) {
		WaveriderConfig wconf = new WaveriderConfig();
		wconf.setPort(config.getPort());
		wconf.setMasterAddress(config.getMasterAddress());
		return wconf;
	}
	
	/**
	 *
	 */
	private class SlaveListenterSupport implements SlaveListenter {

		@Override
		public void joined(SlaveWorker slave) {
			logger.warn(String.format("New slave:%s joined !!!", slave));
		}

		@Override
		public void left(SlaveWorker slave) {
			logger.warn(String.format("Slave:%s left !!!", slave));
			// 释放该Slave占有的Slave
			jobManager.releaseDoingJob(slave);
		}
	}
}
