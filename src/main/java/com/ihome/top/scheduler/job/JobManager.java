package com.ihome.top.scheduler.job;

import com.ihome.top.scheduler.exception.JobKeyDuplicatedException;
import com.ihome.top.scheduler.exception.JobNotFoundException;
import com.ihome.top.scheduler.exception.SchedulerException;
import com.ihome.top.scheduler.job.impl.JobKey;

/**
 * 
 * @author sihai
 * 
 */
public interface JobManager {
	
	/**
	 * 新建默认的job(DefaultJob)
	 * 
	 * @param key
	 * @return
	 * @throws JobKeyDuplicatedException
	 */
	Job newJob(JobKey key) throws JobKeyDuplicatedException;

	/**
	 * 新建Dependency的job(DependencyJob)
	 * 
	 * @param key
	 * @return
	 * @throws JobKeyDuplicatedException
	 */
	Job newDependencyJob(JobKey key) throws JobKeyDuplicatedException;

	/**
	 * 添加新的job，添加到 waitingQueue
	 * 
	 * @param job
	 * @throws SchedulerException
	 */
	void addJob(Job job) throws SchedulerException;

	/**
	 * 获取未完成的job的个数
	 * 
	 * @return
	 */
	public int getUndoJobCount();
	
	/**
	 * 获取完成的job的个数
	 * @return
	 */
	public int getDoneJobCount();

	/**
	 * 获取完成的job的个数
	 * 
	 * @return
	 */
	public int getDoingJobCount();
	
	/**
	 * 移除任务
	 * @param key
	 * @throws JobNotFoundException
	 * @return
	 */
	public Job removeJob(JobKey key) throws JobNotFoundException;
	
	/**
	 * 重新加载所有任务
	 * 清除所有处于非执行状态的任务, 处于执行状态的任务会被最后执行一次, 执行完成后被清理
	 */
	void reloadJob();
	
}
