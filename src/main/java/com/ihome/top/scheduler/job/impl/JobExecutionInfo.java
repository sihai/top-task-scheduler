/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.impl;

import java.io.Serializable;
import java.util.Date;

import com.ihome.top.scheduler.exception.JobExecutionException;
import com.ihome.top.scheduler.job.JobResult;
import com.ihome.top.scheduler.util.DateUtil;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 任务执行情况
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class JobExecutionInfo implements Serializable {

	private static final long serialVersionUID = -639102423867347167L;
	
	private Long jobId;					// 对应任务的内部ID
	private Boolean isSucceed;			// 任务执行是否成功, 系统级别
	private JobExecutionException e;	// 任务执行异常信息
	private JobResult jobResult;		// 业务的任务执行结果
	
	private SlaveWorker worker;			// 本次Slave机器
	private Date dispatchedTime;		// 任务被分发的时间, Master端
	private Date endTime; 				// 任务这次执行完毕时间, Master端收到结果
	private Date slaveReceivedTime;		// Slave接收到任务的时间, Slave端
	private Date slaveJobStartTime;		// 任务开始执行时间, Slave端
	private Date slaveJobEndTime;		// 任务执行完毕时间, Slave端

	public JobExecutionException getExecutionException() {
		return e;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setExecutionException(JobExecutionException e) {
		this.e = e;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
	
	public Boolean isSucceed() {
		return isSucceed;
	}

	public void setIsSucceed(Boolean isSucceed) {
		this.isSucceed = isSucceed;
	}
	
	public JobResult getJobResult() {
		return jobResult;
	}

	public void setJobResult(JobResult jobResult) {
		this.jobResult = jobResult;
	}
	
	public SlaveWorker getWorker() {
		return worker;
	}

	public void setWorker(SlaveWorker worker) {
		this.worker = worker;
	}
	
	public Date getDispatchedTime() {
		return dispatchedTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Date getSlaveReceivedTime() {
		return slaveReceivedTime;
	}

	public Date getSlaveJobStartTime() {
		return slaveJobStartTime;
	}

	public Date getSlaveJobEndTime() {
		return slaveJobEndTime;
	}

	public void setDispatchedTime(Date dispatchedTime) {
		this.dispatchedTime = dispatchedTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setSlaveReceivedTime(Date slaveReceivedTime) {
		this.slaveReceivedTime = slaveReceivedTime;
	}

	public void setSlaveJobStartTime(Date slaveJobStartTime) {
		this.slaveJobStartTime = slaveJobStartTime;
	}

	public void setSlaveJobEndTime(Date slaveJobEndTime) {
		this.slaveJobEndTime = slaveJobEndTime;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(worker);
		sb.append(",");
		sb.append(isSucceed);
		sb.append(",");
		sb.append(dispatchedTime);
		sb.append(",");
		sb.append(DateUtil.format(endTime));
		sb.append(",");
		sb.append(DateUtil.format(slaveReceivedTime));
		sb.append(",");
		sb.append(DateUtil.format(slaveJobStartTime));
		sb.append(",");
		sb.append(DateUtil.format(slaveJobEndTime));
		return sb.toString();
	}
}
