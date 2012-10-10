/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;

/**
 * <p>
 * 当任务在Slave端被取消, Slave回调该接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobCanceledHandler {
	
	/**
	 * 任务被取消回调
	 * @param job
	 */
	void handle(Job job);
}
