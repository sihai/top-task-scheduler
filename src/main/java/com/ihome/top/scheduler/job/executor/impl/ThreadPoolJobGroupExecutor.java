/**
 * top-task-scheduler
 *  
 */
package com.ihome.top.scheduler.job.executor.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.common.SchedulerThreadFactory;
import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.job.JobCompletedReporter;
import com.ihome.top.scheduler.job.JobConsumer;
import com.ihome.top.scheduler.job.executor.AbstractJobGroupExecutor;
import com.ihome.top.scheduler.job.executor.JobGroupExecutorBusyException;
import com.ihome.top.scheduler.job.internal.Job;

/**
 * <p>
 * 基于线程池的任务组执行单元
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class ThreadPoolJobGroupExecutor extends AbstractJobGroupExecutor {

	private static final Log logger = LogFactory.getLog(ThreadPoolJobGroupExecutor.class);
	
	private BlockingQueue<Runnable> workQueue;		// 任务队列
	private ThreadPoolExecutor threadPool;			// 线程池
	
	public ThreadPoolJobGroupExecutor(final String group, JobConsumer jobConsumer, ResourceConfig rconfig) {
		super(group, rconfig, jobConsumer);
		workQueue = new LinkedBlockingQueue<Runnable>(rconfig.getMaxQueueSize());
		threadPool = new ThreadPoolExecutor(rconfig.getMinThread(), rconfig.getMaxThread(), 120, TimeUnit.SECONDS, workQueue, new SchedulerThreadFactory("Top-Task-Scheduler-Job-Consumer-Group(" + group + ")-ThreadPool", null, true));
	}
	
	@Override
	public void execute(Job job, JobCompletedReporter reporter) throws JobGroupExecutorBusyException {
		try {
			threadPool.execute(new SyncJobTask(job, reporter));
			logger.info(String.format("threadPoolJobGroupExecutor for group:%s , corePoolSize:%d, maximumPoolSize:%d, largestPoolSize:%d, activeCount:%d, maxQueueSize:%d, queueSize:%d", this.getGroup(), threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize(), threadPool.getLargestPoolSize(), threadPool.getActiveCount(), this.getResourceConfig().getMaxQueueSize(), workQueue.size()));
		} catch (RejectedExecutionException e) {
			throw new JobGroupExecutorBusyException(e);
		}
 	}

	@Override
	public int availableCapacity() {
		// FIX one bug, availableCapacity = workQueue.remainingCapacity() + this.getResourceConfig().getMaxThread() - threadPool.getPoolSize()
		// 这样才可能让线程池增加线程, 不然workQueue不满, 永远都不会超过corePoolSize
		return workQueue.remainingCapacity() + this.getResourceConfig().getMaxThread() - threadPool.getPoolSize();
	}

	@Override
	public void shutdownExecute() {
		threadPool.shutdown();
		workQueue.clear();
	}

	@Override
	public void shutdownExecuteNow() {
		threadPool.shutdownNow();
		workQueue.clear();
	}
}
