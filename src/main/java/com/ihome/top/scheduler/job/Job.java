/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job;

import java.io.Serializable;
import java.util.Date;

import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.plan.JobExecutePlan;
import com.ihome.top.scheduler.strategy.JobCallbackConditionEnum;
import com.ihome.top.scheduler.strategy.JobCallbackStrategy;
import com.ihome.top.scheduler.strategy.JobReloadConditionEnum;
import com.ihome.top.scheduler.strategy.JobReloadStrategy;
import com.ihome.top.waverider.SlaveWorker;


/**
 * Job.java
 * @author yunzhan.jtq
 * 
 * @since 2011-5-21 下午03:32:24
 */
public interface Job extends Serializable {
   
	/**
     * 获取Job的Id, 内部使用的ID
     * @return
     */
    Long getId();
    
	/**
	 * 获取任务的当前状态
	 * @return
	 */
	JobStatusEnum getStatus();
	
	/**
     * 返回任务全局主键
     * @return
     */
    JobKey getKey();
    
    /**
     * 设置任务全局主键
     * @param key
     */
    void setKey(JobKey key);
    
    /**
     * 设置任务全局主键
     * @param name
     * @param group
     */
    void setKey(String name, String group);
    
    /**
	 * 获取任务执行超时, Master端超时时间, 单位毫秒
	 * 
	 * @return
	 */
	Long getTimeout();
	
	/**
	 * 设置任务执行超时, Master端超时时间, 单位毫秒, 请考虑系统的消耗, 估计在1-2 minutes
	 */
	void setTimeout(Long timeout);
	
	/**
	 * 获取任务最后一次执行情况 
	 * @return
	 */
	JobExecutionInfo getJobExecutionInfo();
	
	 /**
     * 获取Job的内容
     * @return
     */
    JobContent getJobContent();
    
    /**
     * 用户设置的业务相关内容
     * 
     * @param jobContent
     */
    void setJobContent(JobContent jobContent);
    
    /**
     * 获取执行结果
     * @return
     */
    JobResult getJobResult();
    
    /**
     * 
     * @return
     */
    Long getExecutedCount();
    
    /**
     * 设置Job执行计划
     * 
     * @param plan
     */
    void setExecutePlan(JobExecutePlan plan);
    
    /**
     * 获取Job执行计划
     * @return
     */
    JobExecutePlan getExecutePlan();
    
    /**
     * 添加依赖的job
     * 
     * @param job
     */
    void addDependedJob(Job job);
    
    /**
     * 获取任务下一次执行时间
     * @return
     */
    Date getNextExecuteTime();
    
    /**
     * 
     * @return
     */
    SlaveWorker getWorker();
    
    /**
     * 
     * @return
     */
    Long getTimeoutCount();
    
    /**
     * 设置callback策略
     * @param strategy
     */
    void setCallbackStrategy(JobCallbackStrategy strategy);
    
    /**
     * 获取callback策略
     * @return
     */
    JobCallbackStrategy getCallbackStrategry();
    
    /**
     * 获取回调的场景
     * @return
     */
    JobCallbackConditionEnum getCallbackCondition();
    
    /**
     * 设置reload策略
     * @param strategy
     */
    void setReloadStrategy(JobReloadStrategy strategy);
    
    /**
     * 获取reload策略
     * @return
     */
    JobReloadStrategy getReloadStrategry();
    
    /**
     * 获取reload的场景
     * @return
     */
    JobReloadConditionEnum getReloadCondition();
}
