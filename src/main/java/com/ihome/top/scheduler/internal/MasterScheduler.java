/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.internal;

import java.util.Collection;
import java.util.List;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 任务调度Master实例节点内部接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface MasterScheduler extends com.ihome.top.scheduler.MasterScheduler {
	
	/**
	 * 任务完成
	 * @param jobExecutionInfo
	 * @return
	 */
	Job completed(JobExecutionInfo jobExecutionInfo);
	
	/**
	 * 分配任务,可能阻塞
	 * @param requestList
	 * @param worker
	 * @return
	 */
	Collection<Job> dispatch(List<PullJobRequest> requestList, SlaveWorker worker);
	
	/**
	 * 无阻塞分配任务
	 * @param requestList
	 * @param maxWaitingTime
	 * @param worker
	 * @return
	 */
	Collection<Job> dispatch(List<PullJobRequest> requestList, long maxWaitingTime, SlaveWorker worker);
}
