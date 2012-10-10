/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.impl;

import java.util.HashMap;
import java.util.Map;

import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobContent;
import com.ihome.top.scheduler.job.JobResult;

/**
 * <p>
 * 作为连接job之间的桥梁
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 * 
 */
public class MultiJobResultJobContent implements JobContent {

	private static final long serialVersionUID = 287392779984882927L;

	private Map<Long, JobResult> jobResultMap = new HashMap<Long, JobResult>();

	public JobResult get(int jobId) {
		return jobResultMap.get(jobId);
	}

	public void add(Job job) {
		jobResultMap.put(job.getId(), job.getJobResult());
	}

	public void add(Long id, JobResult jobResult) {
		jobResultMap.put(id, jobResult);
	}
}
