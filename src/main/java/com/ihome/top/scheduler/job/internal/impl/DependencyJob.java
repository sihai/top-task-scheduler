/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.internal.impl;

import java.util.LinkedList;
import java.util.List;

import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.job.impl.MultiJobResultJobContent;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 依赖job的实现，一个job依赖多个job的jobRestul作为自己的jobContent
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 * 
 */
public class DependencyJob extends DefaultJob implements Job {

	private static final long serialVersionUID = 8626640186630978090L;

	private List<com.ihome.top.scheduler.job.Job> dependedJobList = new LinkedList<com.ihome.top.scheduler.job.Job>();

	public DependencyJob(Long id , JobKey key) {
		super(id, key);
	}
	
	@Override
	public void dispatch(SlaveWorker worker) {
		super.dispatch(worker);

		MultiJobResultJobContent jobContent = new MultiJobResultJobContent();
		for (com.ihome.top.scheduler.job.Job job : dependedJobList) {
			jobContent.add(job);
		}

		setJobContent(jobContent);
	}

	@Override
	public void addDependedJob(com.ihome.top.scheduler.job.Job job) {
		dependedJobList.add(job);
	}

	@Override
	public boolean isDependencySatisfied() {
		for (com.ihome.top.scheduler.job.Job job : dependedJobList) {
			if (job.getStatus() != JobStatusEnum.JOB_STATUS_COMPLETED) {
				return false;
			}
		}

		return true;
	}
}
