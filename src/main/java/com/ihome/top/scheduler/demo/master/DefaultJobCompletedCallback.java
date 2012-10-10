/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.demo.master;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobCompletedCallback;
import com.ihome.top.scheduler.job.JobManager;

/**
 * <p>
 * Master任务执行完毕回调demo
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobCompletedCallback implements JobCompletedCallback {
	private final static Log logger = LogFactory.getLog(DefaultJobCompletedCallback.class);
	
	@Override
	public void callback(JobManager jobManager, Job job) {
		logger.info(new StringBuilder("Job completed: id=").append(job.getId()));
	}

	@Override
	public void callback(JobManager jobManager, List<Job> jobList) {
		for(Job job : jobList)
		{
			callback(jobManager, job);
		}
	}
}
