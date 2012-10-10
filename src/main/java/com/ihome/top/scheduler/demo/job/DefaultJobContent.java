/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.demo.job;

import com.ihome.top.scheduler.job.JobContent;

/**
 * <p>
 * 系统demo用的JobContent
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */

public class DefaultJobContent implements JobContent {

	private static final long serialVersionUID = -4028034221967753183L;
	
	private String content;
	
	public DefaultJobContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("content:").append(content);
		return sb.toString();
	}
}
