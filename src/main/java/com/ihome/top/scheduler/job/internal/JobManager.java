/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.job.internal;

import java.util.Collection;
import java.util.List;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.common.LifeCycle;
import com.ihome.top.scheduler.job.JobCompletedCallback;
import com.ihome.top.scheduler.job.JobExecutedCallback;
import com.ihome.top.scheduler.job.JobFailedHandler;
import com.ihome.top.scheduler.job.JobProvider;
import com.ihome.top.scheduler.job.JobReloadHandler;
import com.ihome.top.scheduler.job.JobTimeoutHandler;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.plan.internal.JobExecutePlanReadyObserver;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * top-task-scheduler内部的任务管理接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobManager extends LifeCycle, com.ihome.top.scheduler.job.JobManager, JobExecutePlanReadyObserver {
	
	/**
	 * 添加任务产生器
	 * @param jobProvider
	 */
	void addJobProvider(JobProvider jobProvider);
	
	/**
	 * 添加多个任务产生器
	 * @param jobProviderCollection
	 */
	void addAllJobProvider(Collection<JobProvider> jobProviderCollection);
	
	/**
	 * 添加任务产生器
	 * @param handler
	 */
	void addJobReloadHandler(JobReloadHandler handler);
	
	/**
	 * 添加多个任务产生器
	 * @param handlerCollection
	 */
	void addAllJobReloadHandler(Collection<JobReloadHandler> handlerCollection);
	
	/**
	 * 添加任务执行失败处理器
	 * @param handler
	 */
	void addJobFailedHandler(JobFailedHandler handler);
	
	/**
	 * 添加多个任务失败处理器
	 * @param handlerCollection
	 */
	void addAllJobFailedHandler(Collection<JobFailedHandler> handlerCollection);
	
	/**
	 * 添加任务超时处理器
	 * @param handler
	 */
	void addJobTimeoutHandler(JobTimeoutHandler handler);
	
	/**
	 * 添加多个任务超时处理器
	 * @param handlerCollection
	 */
	void addAllJobTimeoutHandler(Collection<JobTimeoutHandler> handlerCollection);
	
	/**
	 * 添加任务执行成功回调
	 * @param handler
	 */
	void addJobExecutedCallback(JobExecutedCallback callback);
	
	/**
	 * 添加多个任务成功回调
	 * @param handlerCollection
	 */
	void addAllJobExecutedCallback(Collection<JobExecutedCallback> callbackCollection);
	
	/**
	 * 添加任务完成回调
	 * @param handler
	 */
	void addJobCompletedCallback(JobCompletedCallback callback);
	
	/**
	 * 添加多个任务完成回调
	 * @param handlerCollection
	 */
	void addAllJobCompletedCallback(Collection<JobCompletedCallback> callbackCollection);
	
	/**
	 * 开启任务reload
	 */
	void enableJobReload();

	/**
	 * 关闭任务reload
	 */
	void closeJobReload();
	
	/**
	 * 开启任务callback
	 */
	void enableJobCallback();

	/**
	 * 关闭任务callback
	 */
	void closeJobCallback();
	
	/**
	 * 开启任务执行日志功能
	 * @param logDirectory
	 * @param fileName
	 */
	void enableJobExecutionLog(String logDirectory, String fileName);
	
	/**
	 * 关闭任务执行日志功能
	 */
	void closeJobExecutionLog();

	/**
	 * 任务被执行了
	 * @param jobExecutionInfo
	 * @return
	 */
	Job executed(JobExecutionInfo jobExecutionInfo);
	
	/**
	 * 分配任务,可能阻塞
	 * @param requestList
	 * @param worker
	 * @return
	 */
	Collection<Job> dispatch(List<PullJobRequest> requestList, SlaveWorker worker);
	
	/**
	 * 无阻塞分配任务
	 * @param requestList
	 * @param maxWaitingTime
	 * @param worker
	 * @return
	 */
	Collection<Job> dispatch(List<PullJobRequest> requestList, long maxWaitingTime, SlaveWorker worker);
	
	/**
	 * 释放该Slave占有的任务
	 * @param worker
	 */
	void releaseDoingJob(SlaveWorker worker);
}
