/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.job;

import java.util.List;

/**
 * <p>
 * 当任务被执行一次后就会回调
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobExecutedCallback {
	
	/**
	 * 回调
	 * @param jobManager
	 * @param job
	 */
	void callback(JobManager jobManager, Job job);
	
	/**
	 * 回调
	 * @param jobManager
	 * @param jobList
	 */
	void callback(JobManager jobManager, List<Job> jobList);
}
