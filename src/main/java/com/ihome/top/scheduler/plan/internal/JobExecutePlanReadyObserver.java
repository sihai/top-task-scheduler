/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.plan.internal;

import com.ihome.top.scheduler.plan.JobExecutePlan;

/**
 * <p>
 * 任务执行计划就绪观察者接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobExecutePlanReadyObserver {
	
	/**
	 * 主题通知方法, 指定任务就绪,通过这个方法通知
	 * @param plan
	 */
	void ready(JobExecutePlan plan);
}
