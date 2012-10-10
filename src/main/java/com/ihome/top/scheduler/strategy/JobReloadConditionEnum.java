/**
 * top-task-scheduler
 *  
 */
package com.ihome.top.scheduler.strategy;

/**
 * <p>
 * 任务重置条件
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum JobReloadConditionEnum {
	
	JOB_RELOAD_CONDITION_WHEN_JOB_EXECUTED(1, "Reload when executed"),
	JOB_RELOAD_CONDITION_WHEN_JOB_COMPLETED(2, "Reload when completed"),
	JOB_RELOAD_CONDITION_WHEN_JOB_TIMEOUT(4, "Reload when job execute timeout"),
	JOB_RELOAD_CONDITION_WHEN_JOB_FAILED(8, "Reload when job execute failed");
	
	int 	value;		// 值
	String 	desc;		// 描述
	
	private JobReloadConditionEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public static JobReloadConditionEnum toEnum(int value) {
		for(JobReloadConditionEnum e : JobReloadConditionEnum.values()) {
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
