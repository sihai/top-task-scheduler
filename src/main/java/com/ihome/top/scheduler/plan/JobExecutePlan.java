/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.plan;

import java.io.Serializable;

import com.ihome.top.scheduler.job.JobPriorityEnum;

/**
 * <p>
 * 任务执行计划
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobExecutePlan extends Serializable {
		
	/**
	 * 获取优先级
	 * @return
	 */
	JobPriorityEnum getPriority();
	
	/**
	 * 设置优先级
	 * @param priority
	 */
	void setPriority(JobPriorityEnum priority);
}
