/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.strategy;


/**
 * <p>
 * 任务重置策略工厂
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobReloadStrategyFactory {
	
	private int conditions = 0;
	
	public static final JobReloadStrategyFactory newFactory() {
		return new JobReloadStrategyFactory();
	}
	
	/**
	 * 
	 * @return
	 */
	public JobReloadStrategyFactory whenJobExecuted() {
		conditions |= JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_EXECUTED.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobReloadStrategyFactory whenJobCompleted() {
		conditions |= JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_COMPLETED.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobReloadStrategyFactory whenJobTimeout() {
		conditions |= JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_TIMEOUT.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobReloadStrategyFactory whenJobFailed() {
		conditions |= JobReloadConditionEnum.JOB_RELOAD_CONDITION_WHEN_JOB_FAILED.value();
		return this;
	}
	
	public JobReloadStrategy build() {
		JobReloadStrategy jobReloadStrategy = new JobReloadStrategy(conditions);
		return jobReloadStrategy;
	}
}
