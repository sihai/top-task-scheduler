/**
 * top-task-scheduler
 *  
 */
package com.ihome.top.scheduler.strategy;

/**
 * <p>
 * 任务回调条件
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum JobCallbackConditionEnum {
	
	JOB_CALLBACK_CONDITION_WHEN_JOB_EXECUTED(1, "Callback when executed"),
	JOB_CALLBACK_CONDITION_WHEN_JOB_COMPLETED(2, "Callback when completed"),
	JOB_CALLBACK_CONDITION_WHEN_JOB_TIMEOUT(4, "Callback when job execute timeout"),
	JOB_CALLBACK_CONDITION_WHEN_JOB_FAILED(8, "Callback when job execute failed");
	
	int 	value;		// 值
	String 	desc;		// 描述
	
	private JobCallbackConditionEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public static JobCallbackConditionEnum toEnum(int value) {
		for(JobCallbackConditionEnum e : JobCallbackConditionEnum.values()) {
			if(e.value == value) {
				return e;
			}
		}
		return null;
	}
	
	public int value() {
		return value;
	}
	
	public String desc() {
		return desc;
	}
}
