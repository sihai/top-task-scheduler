/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.demo.job;

import com.ihome.top.scheduler.job.JobResult;

/**
 * <p>
 * 系统demo用的JobResult
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */

public class DefaultJobResult implements JobResult {

	private static final long serialVersionUID = -5486100484553497735L;
	
	private String result;
	
	public DefaultJobResult(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("result:").append(result);
		return sb.toString();
	}
}
