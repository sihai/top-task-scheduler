/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.demo;

import com.ihome.top.scheduler.MasterScheduler;
import com.ihome.top.scheduler.Scheduler;
import com.ihome.top.scheduler.SlaveScheduler;
import com.ihome.top.scheduler.common.RuningModeEnum;
import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.demo.master.DefaultJobCompletedCallback;
import com.ihome.top.scheduler.demo.master.DefaultJobExecutedCallback;
import com.ihome.top.scheduler.demo.master.DefaultJobFailedHandler;
import com.ihome.top.scheduler.demo.master.DefaultJobProvider;
import com.ihome.top.scheduler.demo.master.DefaultJobReloadHandler;
import com.ihome.top.scheduler.demo.master.DefaultJobTimeoutHandler;
import com.ihome.top.scheduler.demo.slave.DefaultJobConsumer;
import com.ihome.top.scheduler.factory.SchedulerFactory;
import com.ihome.top.scheduler.job.JobConsumer;

/**
 * <p>
 * 系统Demo入口
 * </p>
 * <p>
 * 启动方式：
 * </br>
 * <b>
 * Master:	java Main -mode master
 * </b>
 * </br>
 * <b>
 * Slave:	java Main -mode slave
 * </b>
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class Main {
	
	public static final String GROUP_TEST_0 = "test_group_0";
	public static final String GROUP_TEST_1 = "test_group_1";
	
	private static final int MIN_ARGS_LENGTH = 2;
	private static final String ARG_MODE_KEY = "-mode";
	private static final String MASTER_MODE = "master";
	private static final String SLAVE_MODE = "slave";

	private static Scheduler scheduler;
	private static RuningModeEnum runingMode;

	public static void init() {

		if (runingMode == RuningModeEnum.RUNING_MODE_MASTER) {
			MasterScheduler master = SchedulerFactory.newMasterInstance();
			// 设置Master
			// 添加JobProvider,支持添加多个
			master.addJobProvider(new DefaultJobProvider());
			// 任务重置
			master.addJobReloadHandler(new DefaultJobReloadHandler());
			// 任务执行一次后回调
			master.addJobExecutedCallback(new DefaultJobExecutedCallback());
			// 添加JobCompletedCallback,支持添加多个
			master.addJobCompletedCallback(new DefaultJobCompletedCallback());
			// 添加JobFailedHandler,支持添加多个
			master.addJobFailedHandler(new DefaultJobFailedHandler());
			// 添加JobTimeoutHandler,支持添加多个
			master.addJobTimeoutHandler(new DefaultJobTimeoutHandler());
			// 开启任务reload, 任务reload会根据任务设定的reload策略进行reload
			master.enableJobReload();
			// 开启任务callback, 任务reload会根据任务设定的callback策略进行callback
			master.enableJobCallback();
			// 开启任务执行日志
			master.enableJobExecutionLog("/home/admin", "job.log");
			scheduler = master;
		} else if (runingMode == RuningModeEnum.RUNING_MODE_SLAVE) {
			SlaveScheduler slave = SchedulerFactory.newSlaveInstance("10.232.12.62");
			// 设置Slave
			// 第一个任务组的JobConsumer
			JobConsumer test0JobConsumer = new DefaultJobConsumer();
			// 第一个任务分组执行单元资源配置
			ResourceConfig test0Rconfig = new ResourceConfig();
			// 执行单元任务队列最大容量
			test0Rconfig.setMaxQueueSize(1024 * 8);
			// 执行单元最大线程个数
			test0Rconfig.setMaxThread(64);
			// 执行单元最小线程个数
			test0Rconfig.setMinThread(32);
			slave.addJobConsumer(GROUP_TEST_0, test0JobConsumer, test0Rconfig);
			// 第二个JobConsumer
			JobConsumer test1JobConsumer = new DefaultJobConsumer();
			ResourceConfig test1Rconfig = new ResourceConfig();
			test1Rconfig.setMaxQueueSize(1024 * 8);
			test1Rconfig.setMaxThread(64);
			test1Rconfig.setMinThread(32);
			slave.addJobConsumer(GROUP_TEST_1, test1JobConsumer, test1Rconfig);
			scheduler = slave;
		}

		if (scheduler != null) {
			scheduler.init();
		}
	}

	public static void showUsage() {
		System.out.println("Scheduler");
		System.out.println("	Usage:");
		System.out.println("    	Run as master mode:");
		System.out.println("			java -jar top-task-schedule.jar Main -mode master");
		System.out.println("		Run as slave mode:");
		System.out.println("			java -jar top-task-schedule.jar Main -mode slave");
	}

	public static void main(String[] args) {
		if (args.length < MIN_ARGS_LENGTH) {
			showUsage();
			return;
		}

		if (!ARG_MODE_KEY.equals(args[0])) {
			showUsage();
			return;
		}

		if (MASTER_MODE.equals(args[1])) {
			runingMode = RuningModeEnum.RUNING_MODE_MASTER;
			init();
			runAsMaster();
		} else if (SLAVE_MODE.equals(args[1])) {
			runingMode = RuningModeEnum.RUNING_MODE_SLAVE;
			init();
			runAsSlave();
		} else {
			showUsage();
			return;
		}

		try{ 
			Thread.currentThread().join(); 
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public static void runAsMaster() {
		System.out.println("Run as master mode ......");
		scheduler.start();
	}

	public static void runAsSlave() {
		System.out.println("Run as slave mode ......");
		scheduler.start();
	}
}
