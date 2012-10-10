/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.config;

/**
 * <p>
 * 资源配置
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class ResourceConfig {

	public static final int TOP_TASK_SCHEDULER_RC_MIN_THREAD = 1;
	public static final int TOP_TASK_SCHEDULER_RC_MAX_THREAD = 1;
	public static final int TOP_TASK_SCHEDULER_RC_MAX_QUEUE_SIZE = 32;
	
	// Thread pool config (cpu)
	private int minThread = TOP_TASK_SCHEDULER_RC_MIN_THREAD;			// 最小线程个数, 即便所有的线程处于空闲
	private int maxThread = TOP_TASK_SCHEDULER_RC_MAX_THREAD;			// 最大线程个数
	
	// Queue (memory)
	private int maxQueueSize = TOP_TASK_SCHEDULER_RC_MAX_QUEUE_SIZE;	// 等待队列最大长度
	
	// Asynchronous
	private boolean isAsync = false;
	
	public int getMinThread() {
		return minThread;
	}
	public void setMinThread(int minThread) {
		this.minThread = minThread;
	}
	public int getMaxThread() {
		return maxThread;
	}
	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}
	public boolean isAsync() {
		return isAsync;
	}
	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("minThread:").append(minThread).append(",");
		sb.append("maxThread:").append(maxThread).append(",");
		sb.append("maxQueueSize:").append(maxQueueSize).append(",");
		sb.append("isAsync:").append(isAsync);
		sb.append("}");
		return sb.toString();
	}
}
