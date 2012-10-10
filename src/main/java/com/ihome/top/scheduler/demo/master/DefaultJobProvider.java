/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.demo.master;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.demo.Main;
import com.ihome.top.scheduler.demo.job.DefaultJobContent;
import com.ihome.top.scheduler.exception.JobKeyDuplicatedException;
import com.ihome.top.scheduler.factory.CronJobExecutePlanFactory;
import com.ihome.top.scheduler.factory.SimpleJobExecutePlanFactory;
import com.ihome.top.scheduler.job.Job;
import com.ihome.top.scheduler.job.JobManager;
import com.ihome.top.scheduler.job.JobPriorityEnum;
import com.ihome.top.scheduler.job.JobProvider;
import com.ihome.top.scheduler.job.impl.JobKey;
import com.ihome.top.scheduler.strategy.JobCallbackStrategy;
import com.ihome.top.scheduler.strategy.JobCallbackStrategyFactory;
import com.ihome.top.scheduler.strategy.JobReloadStrategy;
import com.ihome.top.scheduler.strategy.JobReloadStrategyFactory;

/**
 * <p>
 * 系统demo用的JobProvider
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultJobProvider implements JobProvider {
	
	private static final Log logger = LogFactory.getLog(DefaultJobProvider.class);
	
	@Override
	public List<Job> generate(JobManager jobManager) {
		// 任务reload策略, 在执行完成reload, 在执行失败reload, 在执行超时reload
		JobReloadStrategy reloadStrategy = JobReloadStrategyFactory.newFactory().whenJobCompleted().whenJobExecuted().whenJobFailed().whenJobTimeout().build();
		// 任务回调策略, 在执行成功回调, 在
		JobCallbackStrategy callbackStrategy = JobCallbackStrategyFactory.newFactory().whenJobCompleted().whenJobExecuted().whenJobFailed().whenJobTimeout().build();
		List<Job> jobList = new LinkedList<Job>();
		try {
			for(int i = 0; i < 1024 * 32; i++) {
				
				Job job1 = null;
				Job job2 = null;
				Job job3 = null;
				Job job4 = null;
				
				if(i % 2 == 0) {
					job1 = jobManager.newJob(new JobKey("job1_" + i, Main.GROUP_TEST_0));
					job2 = jobManager.newJob(new JobKey("job2_" + i, Main.GROUP_TEST_0));
					job3 = jobManager.newJob(new JobKey("job3_" + i, Main.GROUP_TEST_0));
					job4 = jobManager.newJob(new JobKey("job4_" + i, Main.GROUP_TEST_0));
				} else {
					job1 = jobManager.newJob(new JobKey("job1_" + i, Main.GROUP_TEST_1));
					job2 = jobManager.newJob(new JobKey("job2_" + i, Main.GROUP_TEST_1));
					job3 = jobManager.newJob(new JobKey("job3_" + i, Main.GROUP_TEST_1));
					job4 = jobManager.newJob(new JobKey("job4_" + i, Main.GROUP_TEST_1));
				}
				
				job1.setExecutePlan(SimpleJobExecutePlanFactory.simpleExecutePlan().startAt(new Date()).withRepeatCount(1000000).withIntervalInSeconds(600).withPriority(JobPriorityEnum.JOB_PRIORITY_DEFAULT).build());
				job1.setJobContent(new DefaultJobContent(new StringBuilder(job1.getKey().toString()).append("_jobContent_").append(job1.getExecutePlan()).toString()));
				job1.setReloadStrategy(reloadStrategy);
				job1.setCallbackStrategy(callbackStrategy);
				job2.setExecutePlan(SimpleJobExecutePlanFactory.simpleExecutePlan().startAt(new Date()).withRepeatCount(1000000).withIntervalInSeconds(600).withPriority(JobPriorityEnum.JOB_PRIORITY_DEFAULT).build());
				job2.setJobContent(new DefaultJobContent(new StringBuilder(job2.getKey().toString()).append("_jobContent_").append(job2.getExecutePlan()).toString()));
				job2.setReloadStrategy(reloadStrategy);
				job2.setCallbackStrategy(callbackStrategy);
				job3.setExecutePlan(CronJobExecutePlanFactory.cronExecutePlan().withCron(new StringBuilder("0 */").append(5).append(" * * * ? *").toString()).withPriority(JobPriorityEnum.JOB_PRIORITY_DEFAULT).build());
				job3.setJobContent(new DefaultJobContent(new StringBuilder(job3.getKey().toString()).append("_jobContent_").append(job3.getExecutePlan()).toString()));
				job3.setReloadStrategy(reloadStrategy);
				job3.setCallbackStrategy(callbackStrategy);
				job4.setExecutePlan(CronJobExecutePlanFactory.cronExecutePlan().withCron(new StringBuilder("0 */").append(5).append(" * * * ? *").toString()).withPriority(JobPriorityEnum.JOB_PRIORITY_DEFAULT).build());
				job4.setJobContent(new DefaultJobContent(new StringBuilder(job4.getKey().toString()).append("jobContent_").append(job4.getExecutePlan()).toString()));
				job4.setReloadStrategy(reloadStrategy);
				job4.setCallbackStrategy(callbackStrategy);
				
				jobList.add(job1);
				jobList.add(job2);
				jobList.add(job3);
				jobList.add(job4);
				
				//Job job = jobManager.newDependencyJob();
				
				//job.addDependedJob(job1);
				//job.addDependedJob(job2);
				//job.addDependedJob(job3);
				//job.addDependedJob(job4);
				//job.setExecutePlan(new SimpleJobExecutePlan(now, 100, 120));
				//jobList.add(job);
			}
		} catch (JobKeyDuplicatedException e) {
			logger.error(e);
		}
		
		return jobList;
	}
}
