/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.job;

import java.util.List;

/**
 * <p>
 * 任务完成回调
 * </p>
 * <p>
 * 	<b>注意:</b>
 * 	任务完成的含义指, 任务完全完成, 不会再有任何执行, 比如一个任务定义的如下执行计划:<br/>
 * 	<b>startTime:		2012-02-28 00:00:00		// 首次开始执行时间		<br/></b>
 * 	<b>repeatCount:		100						// 重复执行100次			<br/></b>
 * 	<b>repeatInterval:	60 * 10 				// 重复周期10分钟			<br/></b>
 * 	只有在这个任务的100次执行完毕后才会执行回调
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobCompletedCallback {
	
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
