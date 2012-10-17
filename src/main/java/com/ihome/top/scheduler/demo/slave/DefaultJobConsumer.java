/**
 * top-task-scheduler
 * 
 */
package com.ihome.top.scheduler.demo.slave;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.demo.job.DefaultJobResult;
import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobResult;
import com.ihome.top.scheduler.job.SyncJobConsumer;


/**
 * 
 * demo的任务消费器
 * 
 * @author sihai
 *
 */
public class DefaultJobConsumer implements SyncJobConsumer {
	private final static Log logger = LogFactory.getLog(DefaultJobConsumer.class);
	
	@Override
	public JobResult work(Job job) {
		//logger.info(new StringBuilder("Do job: jobKey = ").append(job.getKey()).append(", jobConent = ").append(job.getJobContent().toString()));
		return new DefaultJobResult(new StringBuilder("Job result of ").append(job.getKey()).toString());
	}
}
