/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;

/**
 * <p>
 * 任务优先级
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum JobPriorityEnum {
	
	JOB_PRIORITY_LOW(0, "Low"),
	JOB_PRIORITY_DEFAULT(5, "Default"),
	JOB_PRIORITY_HIGHT(10, "High");
	
	int 	value;		// 值
	String 	desc;		// 描述
	
	private JobPriorityEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public static JobPriorityEnum toEnum(int value) {
		for(JobPriorityEnum e : JobPriorityEnum.values()) {
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
