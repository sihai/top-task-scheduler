/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;

/**
 * <p>
 * 任务状态
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum JobStatusEnum {
	
	JOB_STATUS_RUNNING(0, "Runing"),
	JOB_STATUS_EXECUTED(1, "Executed"),
	JOB_STATUS_FAILED(2, "Failed"),
	JOB_STATUS_TIMEOUT(3, "Timeout"),
	JOB_STATUS_COMPLETED(4, "Completed"),
	JOB_STATUS_WAITING(5, "Waiting"),
	JOB_STATUS_RELOADING(6, "Reloading"),
	JOB_STATUS_READY(7, "Ready");
	
	int 	value;		// 值
	String 	desc;		// 描述
	
	private JobStatusEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public static JobStatusEnum toEnum(int value) {
		for(JobStatusEnum e : JobStatusEnum.values()) {
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
