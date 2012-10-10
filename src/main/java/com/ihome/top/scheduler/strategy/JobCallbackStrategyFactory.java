/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.strategy;


/**
 * <p>
 * 任务回调策略工厂
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobCallbackStrategyFactory {
	
	private int conditions = 0;
	
	public static final JobCallbackStrategyFactory newFactory() {
		return new JobCallbackStrategyFactory();
	}
	
	/**
	 * 
	 * @return
	 */
	public JobCallbackStrategyFactory whenJobExecuted() {
		conditions |= JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_EXECUTED.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobCallbackStrategyFactory whenJobCompleted() {
		conditions |= JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_COMPLETED.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobCallbackStrategyFactory whenJobTimeout() {
		conditions |= JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_TIMEOUT.value();
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public JobCallbackStrategyFactory whenJobFailed() {
		conditions |= JobCallbackConditionEnum.JOB_CALLBACK_CONDITION_WHEN_JOB_FAILED.value();
		return this;
	}
	
	public JobCallbackStrategy build() {
		JobCallbackStrategy jobCallbackStrategy = new JobCallbackStrategy(conditions);
		return jobCallbackStrategy;
	}
}
