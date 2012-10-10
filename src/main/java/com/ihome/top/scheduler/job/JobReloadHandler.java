/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;

/**
 * <p>
 * 任务重置处理
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobReloadHandler {
	
	/**
	 * 重置任务, 返回新任务, 或是重置后的任务
	 * @param jobManager
	 * @param job
	 * @return
	 */
	Job reload(JobManager jobManager, Job job);
}
