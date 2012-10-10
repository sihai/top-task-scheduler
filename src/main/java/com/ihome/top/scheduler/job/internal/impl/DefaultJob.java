/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.internal.impl;

import java.util.Date;

import com.ihome.top.scheduler.config.SchedulerConfig;
import com.ihome.top.scheduler.job.JobContent;
import com.ihome.top.scheduler.job.JobExecutionContext;
import com.ihome.top.scheduler.job.JobResult;
import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.scheduler.plan.JobExecutePlan;
import com.ihome.top.scheduler.strategy.JobCallbackConditionEnum;
import com.ihome.top.scheduler.strategy.JobCallbackStrategy;
import com.ihome.top.scheduler.strategy.JobCallbackStrategyFactory;
import com.ihome.top.scheduler.strategy.JobReloadConditionEnum;
import com.ihome.top.scheduler.strategy.JobReloadStrategy;
import com.ihome.top.scheduler.strategy.JobReloadStrategyFactory;
import com.ihome.top.scheduler.util.DateUtil;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 默认没有依赖关系的任务实现
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 * 
 */
public class DefaultJob implements Job {

	private static final long serialVersionUID = 6812406758296635177L;

	private Long id; 														// 任务内部ID
	private volatile JobStatusEnum status; 									// 任务状态
	private JobKey key; 													// 任务标识
	private transient Long timeout = SchedulerConfig.DEFAULT_JOB_TIME_OUT; 	// 任务超时时间, Master端, 目前只在Master端有用
	private transient Date nextExecuteTime;									// 任务下一次执行时间
	private transient Date dispatchedTime; 									// 任务最后一次被分发时间, Master端
	private JobExecutionContext jobContext; 								// 任务执行上下文
	private JobContent jobContent; 											// 任务内容, 业务方设置
	private JobExecutionInfo jobExecutionInfo; 								// 任务执行情况
	private transient SlaveWorker worker;			 						// 任务当前被那个Slave占有
	private transient JobExecutePlan plan; 									// 任务执行计划
	private transient Long dispatchedCount; 								// 任务被分发次数
	private volatile transient Long executedCount; 							// 任务被成功执行的次数(系统成功执行)
	private volatile transient Long timeoutCount;							// 任务超时次数
	private volatile transient Long failedCount;							// 任务被执行失败的次数(系统执行失败)
	private volatile transient boolean isRemoved; 							// 移除标记
	private transient JobCallbackStrategy callbackStrategy;					// callback策略
	private transient JobCallbackConditionEnum callbackCondition;			// callback场景
	private transient JobReloadStrategy reloadStrategy;						// reload策略
	private transient JobReloadConditionEnum reloadCondition;				// reload场景
	
	public DefaultJob(Long id, JobKey key) {
		this.id = id;
		this.key = key;
		this.status = JobStatusEnum.JOB_STATUS_WAITING;
		this.failedCount = 0L;
		this.timeoutCount = 0L;
		this.dispatchedCount = 0L;
		this.executedCount = 0L;
		this.isRemoved = false;
		this.callbackStrategy = JobCallbackStrategyFactory.newFactory().build();	// 默认不回调
		this.reloadStrategy = JobReloadStrategyFactory.newFactory().build();		// 默认不reload
	}
	
	@Override
	public JobStatusEnum getStatus() {
		return status;
	}

	@Override
	public void setStatus(JobStatusEnum status) {
		this.status = status;
	}

	@Override
	public JobKey getKey() {
		return key;
	}

	@Override
	public void setKey(JobKey key) {
		this.key = key;
	}

	@Override
	public void setKey(String name, String group) {
		this.key = new JobKey(name, group);
	}

	@Override
	public Long getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	@Override
	public void executed(JobExecutionInfo jobExecutionInfo) {
		jobExecutionInfo.setWorker(worker);
		worker = null;
		jobExecutionInfo.setDispatchedTime(dispatchedTime);
		jobExecutionInfo.setEndTime(new Date());
		this.jobExecutionInfo = jobExecutionInfo;
		if(jobExecutionInfo.isSucceed()) {
			executedCount++;
			status = JobStatusEnum.JOB_STATUS_EXECUTED;
		} else {
			failedCount++;
			status = JobStatusEnum.JOB_STATUS_FAILED;
		}
	}

	@Override
	public void dispatch(SlaveWorker worker) {
		this.dispatchedTime = new Date();
		this.worker = worker;
		dispatchedCount++;
		this.status = JobStatusEnum.JOB_STATUS_RUNNING;
	}

	@Override
	public void setJobContent(JobContent jobContent) {
		this.jobContent = jobContent;
	}

	@Override
	public JobContent getJobContent() {
		return jobContent;
	}

	@Override
	public JobExecutionContext getJobContext() {
		return jobContext;
	}

	@Override
	public void setJobContext(JobExecutionContext jobContext) {
		this.jobContext = jobContext;
	}
	
	@Override
	public void setJobExecutionInfo(JobExecutionInfo jobExecutionInfo) {
		this.jobExecutionInfo = jobExecutionInfo;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public JobExecutionInfo getJobExecutionInfo() {
		return jobExecutionInfo;
	}
	
	@Override
	public JobResult getJobResult() {
		return jobExecutionInfo.getJobResult();
	}

	@Override
	public Long getExecutedCount(){
		return executedCount;
	}

	@Override
	public SlaveWorker getWorker() {
		return worker;
	}
	
	@Override
	public void addDependedJob(com.ihome.top.scheduler.job.Job job) {
		throw new UnsupportedOperationException(
				"DefaultJob not supported dependency!!!");
	}

	@Override
	public boolean isDependencySatisfied() {
		return true;
	}

	@Override
	public boolean isRemoved() {
		return isRemoved;
	}

	@Override
	public void markRemoved() {
		isRemoved = true;
	}

	@Override
	public boolean isSatisfied() {
		return true;
	}

	@Override
	public JobExecutePlan getExecutePlan() {
		return plan;
	}

	@Override
	public void setExecutePlan(JobExecutePlan plan) {
		this.plan = plan;
	}

	@Override
	public Date getNextExecuteTime() {
		return nextExecuteTime;
	}

	@Override
	public void setNextExecuteTime(Date nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	
	@Override
	public Long getTimeoutCount() {
		return timeoutCount;
	}

	@Override
	public void incrTimeout() {
		timeoutCount++;
	}
	
	@Override
	public void setCallbackStrategy(JobCallbackStrategy strategy){
		this.callbackStrategy = strategy;
	}

	@Override
	public JobCallbackStrategy getCallbackStrategry(){
		return callbackStrategy;
	}

	@Override
	public boolean isCallback(JobCallbackConditionEnum condition){
		this.callbackCondition = condition;
		return callbackStrategy.isCallback(condition);
	}

	@Override
	public void setReloadStrategy(JobReloadStrategy strategy) {
		this.reloadStrategy = strategy;
	}

	@Override
	public JobReloadStrategy getReloadStrategry() {
		return reloadStrategy;
	}

	@Override
	public JobCallbackConditionEnum getCallbackCondition() {
		return callbackCondition;
	}

	@Override
	public JobReloadConditionEnum getReloadCondition() {
		return reloadCondition;
	}

	@Override
	public boolean isReload(JobReloadConditionEnum condition) {
		this.reloadCondition = condition;
		return reloadStrategy.isReload(condition);
	}
	
	@Override
	public boolean isTimeout() {
		return isTimeout(new Date());
	}

	@Override
	public boolean isTimeout(Date date) {
		if(nextExecuteTime == null) {
			return false;
		}
		return date.getTime() - nextExecuteTime.getTime() >= timeout;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		// 任务基本信息
		sb.append(id);
		sb.append(",");
		sb.append(key);
		sb.append(",");
		sb.append(status.value());
		sb.append(",");
		sb.append(timeout);
		sb.append(",");
		sb.append(isRemoved);
		
		// 执行计划
		sb.append(",");
		sb.append(plan);
		sb.append(",");
		sb.append(DateUtil.format(nextExecuteTime));
		// 执行统计
		sb.append(",");
		sb.append(dispatchedCount);
		sb.append(",");
		sb.append(executedCount);
		sb.append(",");
		sb.append(failedCount);
		sb.append(",");
		sb.append(timeoutCount);
		
		// 本次执行统计
		sb.append(",");
		sb.append(jobExecutionInfo);
		return sb.toString();
	}
}
