/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.demo.master;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobManager;
import com.ihome.top.scheduler.job.JobTimeoutHandler;

/**
 * <p>
 * Master节点任务超时处理器接口实现Demo
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobTimeoutHandler implements JobTimeoutHandler {

	private final static Log logger = LogFactory.getLog(DefaultJobTimeoutHandler.class);
	
	@Override
	public void handle(JobManager jobManager, Job job) {
		logger.error(new StringBuilder("Job key:").append(job.getKey()).append(" timeout"));
	}

	@Override
	public void handle(JobManager jobManager, List<Job> jobList) {
		for(Job job : jobList) {
			handle(jobManager, job);
		}
	}
}
