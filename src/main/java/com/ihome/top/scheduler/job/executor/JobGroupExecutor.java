/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.executor;

import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.job.JobCompletedReporter;
import com.ihome.top.scheduler.job.JobConsumer;
import com.ihome.top.scheduler.job.internal.Job;


/**
 * <p>
 * 任务组执行单元
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface JobGroupExecutor {
	
	/**
	 * 
	 * @param job
	 * @param reporter
	 */
	void doJob(Job job, JobCompletedReporter reporter);
	
	/**
	 * 关闭执行器并释放资源
	 */
	void shutdown();
	
	/**
	 * 立即关闭执行器并释放资源
	 */
	void shutdownNow();
	
	/**
	 * 获取分组名
	 * @return
	 */
	String getGroup();
	
	/**
	 * 获取资源配置
	 * @return
	 */
	ResourceConfig getResourceConfig();
	
	/**
	 * 获取任务执行接口
	 * @return
	 */
	JobConsumer getJobConsumer();
	
	/**
	 * 计算Job分组执行单元的剩余计算能力
	 * @return
	 */
	int remainingCapacity();
	
	/**
	 * 获取已经执行过的任务数量
	 * @return
	 */
	long completed();
}
