/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.job.internal;

import java.util.Date;

import com.ihome.top.scheduler.job.JobExecutionContext;
import com.ihome.top.scheduler.job.JobStatusEnum;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.strategy.JobCallbackConditionEnum;
import com.ihome.top.scheduler.strategy.JobReloadConditionEnum;
import com.ihome.top.waverider.SlaveWorker;

/**
 * <p>
 * 系统内部任务接口
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public interface Job extends com.ihome.top.scheduler.job.Job {
    
    /**
     * 获取Job的Id, 内部使用的ID
     * @return
     */
    void setId(Long id);
    
    /**
     * 设置任务的状态
     * @param status
     */
    void setStatus(JobStatusEnum status);
    
    /**
     * 获取Job的执行上下文环境
     * @return
     */
    JobExecutionContext getJobContext();
    
    /**
     * Job执行上下文环境
     * @param jobContext
     */
    void setJobContext(JobExecutionContext jobContext);
    
    /**
     * 将job分派出去执行
     * @param worker
     */
    void dispatch(SlaveWorker worker);
    
    /**
     * 设置任务执行情况
     * @param jobExecutionInfo
     */
    void setJobExecutionInfo(JobExecutionInfo jobExecutionInfo);
    
    /**
     * job被执行
     * @param jobExecutionInfo
     */
    void executed(JobExecutionInfo jobExecutionInfo);
    
    /**
     * 
     * @return
     */
    boolean isSatisfied();
    
    /**
     * 
     * @return
     */
    boolean isDependencySatisfied();
    
    /**
     * 标记删除任务
     * @param is
     */
     void markRemoved();
     
     /**
      * 返回任务是否被标记为删除
      * @return
      */
     boolean isRemoved();
     
     /**
      * 设置任务下一次执行时间
      * @param nextExecuteTime
      */
     void setNextExecuteTime(Date nextExecuteTime);
     
     /**
      * 增加超时次数
      */
     void incrTimeout();
     
     /**
      * 是否需要callback
      * @param condition
      * @return
      */
     boolean isCallback(JobCallbackConditionEnum condition);
     
     /**
      * 是否需要reload
      * @param condition
      * @return
      */
     boolean isReload(JobReloadConditionEnum condition);
     
     /**
      * 以当前时间判断任务是否超时
      * @return
      */
     boolean isTimeout();
     
     /**
      * 以指定时间判断任务是否超时
      * @param date
      * @return
      */
     boolean isTimeout(Date date);
}
