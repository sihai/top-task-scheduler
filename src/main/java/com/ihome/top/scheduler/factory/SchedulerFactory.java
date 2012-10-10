/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.factory;

import com.ihome.top.scheduler.MasterScheduler;
import com.ihome.top.scheduler.Scheduler;
import com.ihome.top.scheduler.SlaveScheduler;
import com.ihome.top.scheduler.common.RuningModeEnum;
import com.ihome.top.scheduler.config.SchedulerConfig;
import com.ihome.top.scheduler.impl.DefaultMasterScheduler;
import com.ihome.top.scheduler.impl.DefaultSlaveScheduler;

/**
 * <p>
 * 任务调度实例节点工厂类
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class SchedulerFactory {
	
	/**
	 * 以默认的配置创建任务调度Master节点
	 * @return
	 */
	public static MasterScheduler newMasterInstance() {
		return (MasterScheduler)newInstance(RuningModeEnum.RUNING_MODE_MASTER);
	}
	
	/**
	 * 以默认的配置创建任务调度Slave节点
	 * @return
	 */
	public static SlaveScheduler newSlaveInstance(String masterAddress) {
		return newSlaveInstance(masterAddress, SchedulerConfig.DEFAULT_PORT);
	}
	
	public static SlaveScheduler newSlaveInstance(String masterAddress, int port) {
		SchedulerConfig config = new SchedulerConfig();
		config.setRuningMode(RuningModeEnum.RUNING_MODE_SLAVE);
		config.setMasterAddress(masterAddress);
		config.setPort(port);
		return (SlaveScheduler)newInstance(config);
	}
	
	/**
	 * 指定实例节点运行模式, 使用系统默认配置创建任务调度实例节点
	 * @param runingMode
	 * @return
	 */
	public static Scheduler newInstance(RuningModeEnum runingMode) {
		SchedulerConfig config = new SchedulerConfig();
		config.setRuningMode(runingMode);
		return newInstance(config);
	}
	
	/**
	 * 以指定的配置文件创建任务调度实例节点
	 * @param configFile
	 * @return
	 */
	public static Scheduler newInstance(String configFile) {
		return null;
	}

	/**
	 * 使用指定配置创建任务调度实例节点
	 * @param config
	 * @return
	 */
	public static Scheduler newInstance(SchedulerConfig config) {
		
		Scheduler scheduler = null;
		
		if (config == null) {
			throw new IllegalArgumentException("Parameter config must not be null.");
		}
		
		RuningModeEnum mode = config.getRuningMode();
		if (mode == null) {
			throw new IllegalArgumentException("Please set node's runing mode.");
		} else if (mode == RuningModeEnum.RUNING_MODE_MASTER) {
			scheduler = new DefaultMasterScheduler(config);
		} else if (mode == RuningModeEnum.RUNING_MODE_SLAVE) {
			scheduler = new DefaultSlaveScheduler(config);
		}
		
		return scheduler;
	}
}
