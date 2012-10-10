/**
 * top-task-scheduler
 * 
 */


package com.ihome.top.scheduler.plan.internal;

/**
 * <p>
 * 任务执行计划主题
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobExecutePlanSubject {

	/**
	 * 注册对任务执行计划就绪感兴趣的观察者
	 * @param observer
	 */
	void registerPlanReadyObserver(JobExecutePlanReadyObserver observer);
}
