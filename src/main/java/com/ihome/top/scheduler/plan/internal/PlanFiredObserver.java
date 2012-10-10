/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.plan.internal;

import com.ihome.top.scheduler.plan.JobExecutePlan;

/**
 * <p>
 * 执行计划被触发观察者
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface PlanFiredObserver {
	
	/**
	 * 执行计划被触发通知
	 * @param plan
	 */
	void fired(JobExecutePlan plan);
}
