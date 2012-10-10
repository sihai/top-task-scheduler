/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.plan.impl;

import com.ihome.top.scheduler.job.JobPriorityEnum;
import com.ihome.top.scheduler.plan.AbstractJobExecutePlan;
import com.ihome.top.scheduler.plan.JobExecutePlan;


/**
 * <p>
 * Cron表达式任务执行计划
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class CronJobExecutePlan extends AbstractJobExecutePlan {
	
	private static final long serialVersionUID = 9195743498126907128L;
	
	private String cron;			// cron表达式
	
	public CronJobExecutePlan(String cron) {
		this(cron, JobPriorityEnum.JOB_PRIORITY_DEFAULT);
	}
	
	public CronJobExecutePlan(String cron, JobPriorityEnum priority) {
		super(priority);
		this.cron = cron;
	}
	
	public void setCron(String cron) {
		this.cron = cron;
	}
	
	public String getCron() {
		return cron;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cron == null) ? 0 : cron.hashCode());
		result = prime * result + priority.value();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		
		CronJobExecutePlan p = (CronJobExecutePlan) obj;
		if(cron == null) {
			if(p.cron != null) {
				return false;
			}
		}else if(!cron.equals(p.cron)) {
			return false;
		}
		
		if(priority != p.priority) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("cron,");
		sb.append(cron);
		sb.append(",");
		sb.append(priority);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		JobExecutePlan plan0 = new CronJobExecutePlan(new StringBuilder("0 */").append(1).append(" * * * ? *").toString());
		JobExecutePlan plan1 = new CronJobExecutePlan(new StringBuilder("0 */").append(1).append(" * * * ? *").toString());
		
		System.out.println(plan0.hashCode() == plan1.hashCode());
		System.out.println(plan0.equals(plan1));
	}
}
