/**
 * top-task-scheduler
 * 
 */
package com.ihome.top.scheduler;

import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.job.JobConsumer;

/**
 * <p>
 * 任务调度Slave实例节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface SlaveScheduler extends Scheduler {
	
	/**
	 * 
	 */
	int TOP_TASK_SCHEDULER_SLAVE_DONE_JOB_QUEUE_SIZE = 1024 * 32;
	
	/**
	 * 添加任务消耗接口, 属于指定分组的任务将由该接口处理
	 * 
	 * @param group			组名
	 * @param jobConsumer	任务消耗者
	 * @param rconfig		该分组资源配置，线程数，队列大小
	 */
	void addJobConsumer(String group, JobConsumer jobConsumer, ResourceConfig rconfig);
	
	/**
	 * 
	 * @param requestList
	 */
	//void pullJobDone(List<PullJobRequest> requestList);
}
