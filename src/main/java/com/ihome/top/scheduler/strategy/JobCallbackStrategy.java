/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.strategy;


/**
 * <p>
 * 任务回调策略
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobCallbackStrategy {
	
	private int condition = 0;
	
	public JobCallbackStrategy(int condition) {
		this.condition = condition;
	}
	
	public boolean isCallback(JobCallbackConditionEnum condition) {
		return (this.condition & condition.value()) == condition.value();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("JobCallbackStrategy:[");
		for(JobCallbackConditionEnum e : JobCallbackConditionEnum.values()) {
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
