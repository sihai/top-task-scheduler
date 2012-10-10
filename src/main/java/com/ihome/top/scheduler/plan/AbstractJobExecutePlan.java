/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.plan;

import com.ihome.top.scheduler.job.JobPriorityEnum;

/**
 * <p>
 * 执行计划基类
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public abstract class AbstractJobExecutePlan implements JobExecutePlan {

	private static final long serialVersionUID = -8049862392830111348L;

	protected JobPriorityEnum priority;				// 计划优先级
	
	public AbstractJobExecutePlan() {
		this(JobPriorityEnum.JOB_PRIORITY_DEFAULT);
	}
	
	public AbstractJobExecutePlan(JobPriorityEnum priority) {
		this.priority = priority;
	}
	
	@Override
	public JobPriorityEnum getPriority() {
		return priority;
	}

	@Override
	public void setPriority(JobPriorityEnum priority) {
		this.priority = priority;
	}
}
