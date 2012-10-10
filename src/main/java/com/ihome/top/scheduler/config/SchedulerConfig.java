/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.config;

import com.ihome.top.scheduler.common.RuningModeEnum;
import com.ihome.top.waverider.config.WaveriderConfig;

/**
 * <p>
 * 任务调度实例节点配置
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class SchedulerConfig {
	
	public static final int DEFAULT_PORT = WaveriderConfig.WAVERIDER_DEFAULT_PORT;			// Master监听端口
	public static final long DEFAULT_JOB_TIME_OUT = 1000 * 60 * 60;							// 一个小时
	
	// 任务日志
	public static final String DEFAULT_JOB_LOG_DIRECTORY  = "/tmp";							// 任务统计日志目录
	public static final String DEFAULT_JOB_LOG_FILE_NAME  = "job.log";						// 任务统计日志文件名		
	public static final int DEFAULT_JOB_LOG_QUEUE_SIZE  = 128;								// 日志缓冲区大小, 对象引用数
	public static final double DEFAULT_JOB_LOG_QUEUE_THRESHOLD = 0.8D;						// 刷新日志阀值
	
	/**
	 * 节点运行模式
	 */
	private RuningModeEnum runingMode;														// 节点运行模式
	
	public RuningModeEnum getRuningMode() {
		return runingMode;
	}

	public void setRuningMode(RuningModeEnum runingMode) {
		this.runingMode = runingMode;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//											Master
	///////////////////////////////////////////////////////////////////////////////////////////////
	 /**
     * master机器的地址
     */
    private String masterAddress;
    
    /**
     * master机器的端口
     * 默认8206
     */
    private int port = DEFAULT_PORT;
    
    /**
     * 开启Master端任务重置总开关 
     */
    private boolean enableJobReload = false;
    
    /**
     * 开启Master端任务回调总开关 
     */
    private boolean enableJobCallback = false;
    
	///////////////////////////////////////////////////////////////////////////////////////////////
	//											Slave
	///////////////////////////////////////////////////////////////////////////////////////////////
	
    ///////////////////////////////////////////////////////////////////////////////////////////////
	//											Network communication/M-S
	///////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////
	//											Job
	///////////////////////////////////////////////////////////////////////////////////////////////
    private boolean enableLogJobExecution = false;						// 
	private String jobLogDirectory = DEFAULT_JOB_LOG_DIRECTORY;
    private String jobLogFileName = DEFAULT_JOB_LOG_FILE_NAME;
    private int jobLogQueueSize = DEFAULT_JOB_LOG_QUEUE_SIZE;
    private Double jobLogQueueThreshold = DEFAULT_JOB_LOG_QUEUE_THRESHOLD;

    
    public boolean isEnableJobReload() {
		return enableJobReload;
	}

	public void setEnableJobReload(boolean enableJobReload) {
		this.enableJobReload = enableJobReload;
	}
	
	public boolean isEnableJobCallback() {
		return enableJobCallback;
	}

	public void setEnableJobCallback(boolean enableJobCallback){
		this.enableJobCallback = enableJobCallback;
	}
	
	public String getMasterAddress() {
		return masterAddress;
	}

	public void setMasterAddress(String masterAddress) {
		this.masterAddress = masterAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean isEnableLogJobExecution() {
		return enableLogJobExecution;
	}

	public void setEnableLogJobExecution(boolean enableLogJobExecution) {
		this.enableLogJobExecution = enableLogJobExecution;
	}
	
	public String getJobLogDirectory() {
		return jobLogDirectory;
	}

	public void setJobLogDirectory(String jobLogDirectory) {
		this.jobLogDirectory = jobLogDirectory;
	}
	
	public String getJobLogFileName() {
		return jobLogFileName;
	}

	public void setJobLogFileName(String jobLogFileName) {
		this.jobLogFileName = jobLogFileName;
	}
	
	public int getJobLogQueueSize() {
		return jobLogQueueSize;
	}

	public void setJobLogQueueSize(int jobLogQueueSize) {
		this.jobLogQueueSize = jobLogQueueSize;
	}

	public Double getJobLogQueueThreshold() {
		return jobLogQueueThreshold;
	}

	public void setJobLogQueueThreshold(Double jobLogQueueThreshold) {
		this.jobLogQueueThreshold = jobLogQueueThreshold;
	}
}
