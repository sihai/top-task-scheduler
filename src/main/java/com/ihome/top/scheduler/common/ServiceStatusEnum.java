/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.common;

/**
 * <p>
 * 服务状态
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum ServiceStatusEnum {
	SERVICE_INITING(0, "Initing"),
	SERVICE_SERVICEING(1, "Servicing"),
	SERVICE_SHUTDOWNING(2, "Shutdowning"),
	SERVICE_SHUTDOWN(3, "Shutdown");
	
	private int value;
	private String desc;
	
	private ServiceStatusEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public static ServiceStatusEnum toEnum(int value) {
		for(ServiceStatusEnum e : ServiceStatusEnum.values()) {
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
