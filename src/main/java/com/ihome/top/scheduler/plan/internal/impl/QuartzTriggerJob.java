/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.plan.internal.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ihome.top.scheduler.plan.JobExecutePlan;
import com.ihome.top.scheduler.plan.internal.JobExecutePlanManager;

/**
 * <p>
 * 任务触发模块
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class QuartzTriggerJob implements Job {
	
	private static final Log logger = LogFactory.getLog(QuartzTriggerJob.class);
	
	public static final String JOB_DATA_KEY_EXECUTE_PLAN_MGR = "execute_plan_mgr";
	public static final String JOB_DATA_KEY_EXECUTE_PLAN = "execute_plan";
	
	public QuartzTriggerJob() {
		
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//logger.info("Execute one quartz job");
		JobDataMap data = context.getJobDetail().getJobDataMap();
		JobExecutePlanManager planManager = (JobExecutePlanManager)data.get(JOB_DATA_KEY_EXECUTE_PLAN_MGR);
		JobExecutePlan plan = (JobExecutePlan)data.get(JOB_DATA_KEY_EXECUTE_PLAN);
		planManager.fired(plan);
	}
}
