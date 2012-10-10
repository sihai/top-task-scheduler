/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.master;

import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.plan.JobExecutePlan;

/**
 * <p>
 * Master端任务基类, 这些任务会在Master端执行, 不会被分发到Slave
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public abstract class AbstractMasterJob implements MasterJob {

	private Long id;
	private JobKey key;
	private volatile JobStatusEnum status;
	private JobExecutePlan plan;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public JobKey getKey() {
		return key;
	}
	
	@Override
	public void setKey(JobKey key) {
		this.key = key;
	}

	@Override
	public JobStatusEnum getStatus() {
		return status;
	}

	@Override
	public JobExecutePlan getExecutePlan() {
		return plan;
	}
	
	@Override
	public void setExecutePlan(JobExecutePlan plan) {
		this.plan = plan;
	}
}
