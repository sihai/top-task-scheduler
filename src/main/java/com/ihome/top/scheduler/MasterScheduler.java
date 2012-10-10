/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler;

import com.ihome.top.scheduler.job.JobCompletedCallback;
import com.ihome.top.scheduler.job.JobExecutedCallback;
import com.ihome.top.scheduler.job.JobFailedHandler;
import com.ihome.top.scheduler.job.JobManager;
import com.ihome.top.scheduler.job.JobProvider;
import com.ihome.top.scheduler.job.JobReloadHandler;
import com.ihome.top.scheduler.job.JobTimeoutHandler;

/**
 * <p>
 * 任务调度Master实例节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface MasterScheduler extends Scheduler, JobManager {

	/**
	 * 添加任务产生器
	 * 
	 * @param jobProvider
	 */
	void addJobProvider(JobProvider jobProvider);
	
	/**
	 * 添加任务reload器
	 * @param handler
	 */
	void addJobReloadHandler(JobReloadHandler handler);
	
	/**
	 * 添加任务失败处理器
	 * @param handler
	 */
	void addJobFailedHandler(JobFailedHandler handler);
	
	/**
	 * 添加任务超时处理器
	 * @param handler
	 */
	void addJobTimeoutHandler(JobTimeoutHandler handler);
	
	/**
	 * 添加任务执行完毕回调
	 * @param callback
	 */
	void addJobExecutedCallback(JobExecutedCallback callback);
	
	/**
	 * 添加任务执行完毕回调
	 * @param callback
	 */
	void addJobCompletedCallback(JobCompletedCallback callback);
	
	/**
	 * 开启任务回调, 默认关闭
	 */
	void enableJobCallback();
	
	/**
	 * 关闭任务回调
	 */
	void closeJobCallback();
	
	/**
	 * 开启任务reload, 默认关闭
	 */
	void enableJobReload();
	
	/**
	 * 关闭任务reload
	 */
	void closeJobReload();
	
	/**
	 * 立马重新加载任务, 干掉所有的老任务
	 */
	void reloadJob();
	
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
	 * 获取任务管理器
	 * @return
	 */
	JobManager getJobManager();
}
