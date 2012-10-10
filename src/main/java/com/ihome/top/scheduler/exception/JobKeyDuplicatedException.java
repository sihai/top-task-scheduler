/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.exception;

/**
 * <p>
 * 任务Key重复
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobKeyDuplicatedException extends Exception {

	private static final long serialVersionUID = 4635152123495424820L;

	public JobKeyDuplicatedException(Throwable t) {
		super(t);
	}
	
	public JobKeyDuplicatedException(String msg, Throwable t) {
		super(msg, t);
	}

	public JobKeyDuplicatedException(String msg) {
		super(msg);
	}
}