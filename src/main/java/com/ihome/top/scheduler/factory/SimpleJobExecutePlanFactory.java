/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.factory;

import java.util.Date;

import com.ihome.top.scheduler.job.JobPriorityEnum;
import com.ihome.top.scheduler.plan.impl.SimpleJobExecutePlan;

/**
 * <p>
 * SimpleJobExecutePlan任务执行计划工厂
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class SimpleJobExecutePlanFactory {
	private Date startTime = new Date();
	private int repeatCount = 0; 
	private int repeatInterval; 
	private JobPriorityEnum priority = JobPriorityEnum.JOB_PRIORITY_DEFAULT;
	
	/**
	 * 创建SimpleJobExecutePlan工厂
	 * @return
	 */
	public static final SimpleJobExecutePlanFactory simpleExecutePlan() {
		return new SimpleJobExecutePlanFactory();
	}
	
	/**
	 * 设置首次执行时间
	 * @param date
	 * @return
	 */
	public SimpleJobExecutePlanFactory startAt(Date date) {
		this.startTime = date;
		return this;
	}
	
	/**
	 * 设置重复周期，单位秒
	 * @param repeatInterval
	 * @return
	 */
	public SimpleJobExecutePlanFactory withIntervalInSeconds(int repeatInterval) { 
		this.repeatInterval = repeatInterval;
		return this;
	}
	
	/**
	 * 设置重复次数, 重复的次数为repeatCount + 1
	 * @param repeatCount
	 * @return
	 */
	public SimpleJobExecutePlanFactory withRepeatCount(int repeatCount) { 
		this.repeatCount = repeatCount;
		return this;
	}
	
	/**
	 * 设置优先级
	 * @param priority
	 * @return
	 */
	public SimpleJobExecutePlanFactory withPriority(JobPriorityEnum priority) { 
		this.priority = priority;
		return this;
	}
	
	/**
	 * 生成SimpleJobExecutePlan
	 * @return
	 */
	public SimpleJobExecutePlan build() {
		return new SimpleJobExecutePlan(startTime, repeatCount, repeatInterval, priority);
	}
}
