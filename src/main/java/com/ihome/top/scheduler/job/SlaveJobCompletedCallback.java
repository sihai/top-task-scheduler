/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;


/**
 * 一个job完成侯的回调Slave
 * @author raoqiang
 *
 */
public interface SlaveJobCompletedCallback {
	
	/**
	 * 
	 * @param success
	 * @param job
	 * @param result
	 */
	void callback(boolean success, Job job, JobResult result);
}
