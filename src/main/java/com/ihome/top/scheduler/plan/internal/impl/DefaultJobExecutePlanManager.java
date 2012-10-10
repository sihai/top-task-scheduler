/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.plan.internal.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.ihome.top.scheduler.plan.JobExecutePlan;
import com.ihome.top.scheduler.plan.impl.CronJobExecutePlan;
import com.ihome.top.scheduler.plan.impl.SimpleJobExecutePlan;
import com.ihome.top.scheduler.plan.internal.JobExecutePlanManager;
import com.ihome.top.scheduler.plan.internal.JobExecutePlanReadyObserver;

/**
 * <p>
 * 任务执行计划管理模块
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobExecutePlanManager implements JobExecutePlanManager {
	
	private static final Log logger = LogFactory.getLog(DefaultJobExecutePlanManager.class);
	
	private SchedulerFactory schedulerFactory;											// Quartz调度器工厂
	private Scheduler scheduler;														// Quartz调度器
	private volatile List<JobExecutePlanReadyObserver> jobExecutePlanReadyObserverList;	// 任务执行计划就绪观察者列表
	private AtomicLong idGenerator;														// ID生成器, ID会作为Quartz的JobKey和TriggerKey的一部分
	private ConcurrentHashMap<JobExecutePlan, Long> plan2IdMap;							// 执行计划到ID的映射
	
	public DefaultJobExecutePlanManager() {
		this.jobExecutePlanReadyObserverList = new LinkedList<JobExecutePlanReadyObserver>();
		this.idGenerator = new AtomicLong(0L);
		this.plan2IdMap = new ConcurrentHashMap<JobExecutePlan, Long>();
	}
	
	@Override
	public boolean init() {
		try {
			schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();
			return true;
		} catch (SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			return false;
		}
	}

	@Override
	public boolean restart() {
		return true;
	}

	@Override
	public boolean start() {
		try {
			//logger.info("Start quartz ...");
			scheduler.start();
			//logger.info("Start quartz ok");
			return true;
		} catch (SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			return false;
		}
	}

	@Override
	public boolean stop() {
		try {
			scheduler.shutdown();
			jobExecutePlanReadyObserverList.clear();
			this.idGenerator.set(0L);
			plan2IdMap.clear();
			return true;
		} catch (SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			return false;
		}
	}

	@Override
	public void addPlan(JobExecutePlan plan) throws com.ihome.top.scheduler.exception.SchedulerException {
		_add_quartz_job_(plan);
	}

	@Override
	public void removePlan(JobExecutePlan plan) {
		_remove_quartz_job_(plan);
		logger.warn(new StringBuilder("Removed one plan :").append(plan).append("from jobExecutePlanManager").toString());
	}
	
	@Override
	public void removeAllPlan() {
		Iterator<JobExecutePlan> iterator = plan2IdMap.keySet().iterator();
		JobExecutePlan plan = null;
		while(iterator.hasNext()) {
			plan = iterator.next();
			_remove_quartz_job_(plan);
			logger.warn(new StringBuilder("Removed one plan :").append(plan).append("from jobExecutePlanManager").toString());
		}
	}
	
	@Override
	public boolean mayExecuteAgain(JobExecutePlan plan) {
		try {
			Trigger trigger = _find_trigger_for_plan_(plan);
			if(trigger == null) {
				// FIXME
				return false;
			}
			return trigger.mayFireAgain();
		} catch(SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			return false;
		}
	}
	
	@Override
	public Date nextExecuteTime(JobExecutePlan plan) {
		try {
			Trigger trigger = _find_trigger_for_plan_(plan);
			if(trigger == null) {
				// FIXME
				return null;
			}
			return trigger.getNextFireTime();
		} catch(SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			return null;
		}
	}
	
	@Override
	public void registerPlanReadyObserver(JobExecutePlanReadyObserver observer) {
		List<JobExecutePlanReadyObserver> newList = new LinkedList<JobExecutePlanReadyObserver>(this.jobExecutePlanReadyObserverList);
		newList.add(observer);
		this.jobExecutePlanReadyObserverList = newList;
	}
	
	@Override
	public void fired(JobExecutePlan plan) {
		_fire_(plan);
	}
	
	private Trigger _find_trigger_for_plan_(JobExecutePlan plan) throws SchedulerException {
		Long id = this.plan2IdMap.get(plan);
		if(id == null) {
			throw new SchedulerException(new StringBuilder("Plan:").append(plan).append("not exist in JobExecutePlanManager").toString());
		}
		TriggerKey key = makeQuartzTriggerKey(id);
		Trigger trigger = scheduler.getTrigger(key);
		return trigger;
	}
	
	/**
	 * 
	 * @param plan
	 */
	private void _fire_(JobExecutePlan plan) {
		for(JobExecutePlanReadyObserver observer : jobExecutePlanReadyObserverList) {
			observer.ready(plan);
		}
	}
	
	/**
	 * 创建quartz job控制执行计划
	 * @param plan
	 * @throws com.ihome.top.scheduler.exception.SchedulerException
	 */
	private void _add_quartz_job_(JobExecutePlan plan) throws com.ihome.top.scheduler.exception.SchedulerException {
		Long id = this.idGenerator.getAndIncrement();
		Trigger trigger = null;
		TriggerKey triggerKey = makeQuartzTriggerKey(id);
		JobKey jobKey = makeQuartzJobKey(id);
		JobDetail qJob = JobBuilder.newJob(QuartzTriggerJob.class).withIdentity(jobKey).build();
		if(plan instanceof SimpleJobExecutePlan) {
			SimpleJobExecutePlan splan = (SimpleJobExecutePlan)plan;
			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withPriority(plan.getPriority().value()).startAt(splan.getStartTime()).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(splan.getRepeatInterval()).withRepeatCount(splan.getRepeatCount()).withMisfireHandlingInstructionFireNow()).build();
		} else if(plan instanceof CronJobExecutePlan) {
			CronJobExecutePlan cplan = (CronJobExecutePlan)plan;
			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withPriority(plan.getPriority().value()).withSchedule(CronScheduleBuilder.cronSchedule(cplan.getCron()).withMisfireHandlingInstructionFireAndProceed()).build();
		} else {
			throw new com.ihome.top.scheduler.exception.SchedulerException("Unkown Execute Plan");
		}
		
		try {
			qJob.getJobDataMap().put(QuartzTriggerJob.JOB_DATA_KEY_EXECUTE_PLAN, plan);
			qJob.getJobDataMap().put(QuartzTriggerJob.JOB_DATA_KEY_EXECUTE_PLAN_MGR, this);
			scheduler.scheduleJob(qJob, trigger);
			//logger.info(new StringBuilder("Add one quartz job:{qJob=").append(qJob).append(", trigger=").append(trigger).toString());
		} catch(SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			throw new com.ihome.top.scheduler.exception.SchedulerException(e);
		}
		//logger.info("Plan:" + plan);
		//logger.info("Add one trigger, key:" + triggerKey);
		//logger.info("Add one job, key:" + jobKey);
		this.plan2IdMap.put(plan, id);
	}
	
	/**
	 * 移除底层任务执行计划
	 * @param plan
	 */
	private void _remove_quartz_job_(JobExecutePlan plan) {
		try {
			Long id = this.plan2IdMap.get(plan);
			if(id == null) {
				return;
				//throw new com.ihome.top.scheduler.exception.SchedulerException(new StringBuilder("Plan:").append(plan).append("not exist in JobExecutePlanManager").toString());
			}
			
			scheduler.deleteJob(makeQuartzJobKey(id));
			this.plan2IdMap.remove(plan);
		} catch(SchedulerException e) {
			logger.error("OOPS：Exception：", e);
			//throw new com.ihome.top.scheduler.exception.SchedulerException(e);
			return;
		}
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private JobKey makeQuartzJobKey(Long id) {
		return new JobKey("quartz_job_" + String.valueOf(id), JOB_EXECUTE_PLAN_MGR_QUARTZ_GROUP_NAME);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private TriggerKey makeQuartzTriggerKey(Long id) {
		return new TriggerKey("quartz_trigger_" + String.valueOf(id), JOB_EXECUTE_PLAN_MGR_QUARTZ_GROUP_NAME);
	}
}
