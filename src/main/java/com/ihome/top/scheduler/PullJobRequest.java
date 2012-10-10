/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * <p>
 * Slave拉取Job参数
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class PullJobRequest implements Serializable {

	private static final long serialVersionUID = -5400629165399245658L;
	
	private int size;		// 
	private int max;		// 最大job个数
	private String group;	// 任务组名
	
	public PullJobRequest(String group, int max) {
		this.size = 0;
		this.group = group;
		this.max = max;
	}
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getSize() {
		int size = 0;
		size += Integer.SIZE / Byte.SIZE;
		size += Integer.SIZE / Byte.SIZE;
		size += group.getBytes().length;
		return size;
	}
	
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static PullJobRequest unmarshall(ByteBuffer buffer) {
		PullJobRequest request = new PullJobRequest(null, 0);
		request.size = buffer.getInt();
		request.max = buffer.getInt();
		byte[] buf = new byte[request.size - Integer.SIZE / Byte.SIZE - Integer.SIZE / Byte.SIZE];
		buffer.get(buf);
		request.group = new String(buf);
		return request;
	}
	
	/**
	 * 
	 * @param buffer
	 */
	public ByteBuffer marshall() {
		ByteBuffer buffer = ByteBuffer.allocate(getSize());
		buffer.putInt(getSize());
		buffer.putInt(max);
		buffer.put(group.getBytes());
		buffer.flip();
		return buffer;
	}
	
	/**
	 * 
	 * @param buffer
	 */
	public void fillByteBuffer(ByteBuffer buffer) {
		buffer.putInt(getSize());
		buffer.putInt(max);
		buffer.put(group.getBytes());
	}
}
