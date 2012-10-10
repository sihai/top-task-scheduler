/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.job;

import java.util.List;

/**
 * <p>
 * Master节点任务超时处理器接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobTimeoutHandler {
	
	/**
	 * 处理超时任务
	 * @param jobManager
	 * @param job
	 */
	void handle(JobManager jobManager, Job job);
	
	/**
	 * 批量处理超时任务
	 * @param jobManager
	 * @param job
	 */
	void handle(JobManager jobManager, List<Job> jobList);
}
