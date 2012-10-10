/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.factory;

import com.ihome.top.scheduler.job.JobPriorityEnum;
import com.ihome.top.scheduler.plan.impl.CronJobExecutePlan;

/**
 * <p>
 * CronJobExecutePlan任务执行计划工厂
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class CronJobExecutePlanFactory {
	private String cron;
	private JobPriorityEnum priority = JobPriorityEnum.JOB_PRIORITY_DEFAULT;
	
	/**
	 * 创建CronJobExecutePlan工厂
	 * @return
	 */
	public static final CronJobExecutePlanFactory cronExecutePlan() {
		return new CronJobExecutePlanFactory();
	}
	
	/**
	 * 设置Cron表达式
	 * @param repeatCount
	 * @return
	 */
	public CronJobExecutePlanFactory withCron(String cron) { 
		this.cron = cron;
		return this;
	}
	
	/**
	 * 设置优先级
	 * @param priority
	 * @return
	 */
	public CronJobExecutePlanFactory withPriority(JobPriorityEnum priority) { 
		this.priority = priority;
		return this;
	}
	
	/**
	 * 生成SimpleJobExecutePlan
	 * @return
	 */
	public CronJobExecutePlan build() {
		return new CronJobExecutePlan(cron, priority);
	}
}
