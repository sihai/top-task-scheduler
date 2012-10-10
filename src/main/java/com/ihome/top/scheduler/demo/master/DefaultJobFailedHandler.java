/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.demo.master;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobFailedHandler;
import com.ihome.top.scheduler.job.JobManager;

/**
 * <p>
 * Master节点任务失败处理器接口实现Demo
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobFailedHandler implements JobFailedHandler {

	private final static Log logger = LogFactory.getLog(DefaultJobFailedHandler.class);
	
	@Override
	public void handle(JobManager jobManager, Job job) {
		logger.error(new StringBuilder("Job key:").append(job.getKey()).append(" failed, exception:").append(job.getJobExecutionInfo().getExecutionException()));
	}

	@Override
	public void handle(JobManager jobManager, List<Job> jobList) {
		for(Job job : jobList) {
			handle(jobManager, job);
		}
	}
}
