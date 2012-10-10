/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.demo.master;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobManager;
import com.ihome.top.scheduler.job.JobReloadHandler;
import com.ihome.top.scheduler.strategy.JobReloadConditionEnum;

/**
 * <p>
 * Master节点任务失败处理器接口实现Demo
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobReloadHandler implements JobReloadHandler {

	private final static Log logger = LogFactory.getLog(DefaultJobReloadHandler.class);
	
	@Override
	public Job reload(JobManager jobManager, Job job) {
		//logger.warn(new StringBuilder("Reload one job:").append(job.getKey()).append(" for condition:").append(job.getReloadCondition().desc()));
		JobReloadConditionEnum condition = job.getReloadCondition();
		//job.setKey(new JobKey(job.getKey().getName() + "_reload_" + condition.desc(), job.getKey().getGroup()));
		if(condition == JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_COMPLETED) {
			// 完成, 修改了任务再重来一次
			return job;
			// 完成, 结束
			//return null;
			// 完成, 新建一个
			//retturn newJob;
		} else if(condition == JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_EXECUTED) {
			// 执行一次成功, 继续执行
			return job;
			// 不执行了
			//return null;
		} else if(condition == JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_FAILED) {
			// 执行失败
			logger.error("Reload one failed job:" + job.getKey(), job.getJobExecutionInfo().getExecutionException());
			
			// 不执行了
			return null;
		} else if(condition == JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_TIMEOUT) {
			// 执行超时
			job.setTimeout(job.getTimeout() * 2);
		}
		return job;
	}
}
