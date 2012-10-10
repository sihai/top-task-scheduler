/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.strategy;


/**
 * <p>
 * 任务重置策略
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobReloadStrategy {
	
	private int condition = 0;
	
	public JobReloadStrategy(int condition) {
		this.condition = condition;
	}
	
	public boolean isReload(JobReloadConditionEnum condition) {
		return (this.condition & condition.value()) == condition.value();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("JobReloadStrategy:[");
		for(JobReloadConditionEnum e : JobReloadConditionEnum.values()) {
			if((e.value & condition) == e.value) {
				sb.append(e);
				sb.append(",");
			}
		}
		if(sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}
}
