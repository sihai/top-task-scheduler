/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.master;

import com.ihome.top.scheduler.job.JobManager;
import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.plan.JobExecutePlan;

/**
 * <p>
 * Master端任务, 这些任务会在Master端执行, 不会被分发到Slave
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface MasterJob {
	
	/**
	 * 返回任务的内部ID
	 * @return
	 */
	Long getId();
	
	/**
	 * 
	 * @return
	 */
	JobKey getKey();
	
	/**
	 * 
	 * @param key
	 */
	void setKey(JobKey key);
	
	/**
	 * 
	 * @return
	 */
	JobStatusEnum getStatus();
	
	/**
	 * 获取任务执行计划
	 * @return
	 */
	JobExecutePlan getExecutePlan();
	
	/**
	 * 
	 * @param plan
	 */
	void setExecutePlan(JobExecutePlan plan);
	
	/**
	 * 任务执行方法
	 * @param jobManager
	 */
	void execute(JobManager jobManager);
}
