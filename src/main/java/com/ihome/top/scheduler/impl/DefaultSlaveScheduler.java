/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.command.CommandConstants;
import com.ihome.top.scheduler.command.slave.PushJobCommandHandler;
import com.ihome.top.scheduler.config.ResourceConfig;
import com.ihome.top.scheduler.config.SchedulerConfig;
import com.ihome.top.scheduler.job.JobCompletedReporter;
import com.ihome.top.scheduler.job.JobConsumer;
import com.ihome.top.scheduler.job.JobResult;
import com.ihome.top.scheduler.job.executor.JobGroupExecutor;
import com.ihome.top.scheduler.job.executor.impl.ThreadPoolJobGroupExecutor;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.waverider.command.Command;
import com.ihome.top.waverider.command.CommandFactory;
import com.ihome.top.waverider.command.CommandProvider;
import com.ihome.top.waverider.config.WaveriderConfig;
import com.ihome.top.waverider.slave.DefaultSlaveNode;

/**
 * <p>
 * 系统默认的Slave节点
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DefaultSlaveScheduler implements com.ihome.top.scheduler.internal.SlaveScheduler, JobCompletedReporter {

	private static final Log logger = LogFactory.getLog(DefaultSlaveScheduler.class);
	
	private SchedulerConfig config;										// 节点配置信息
	private DefaultSlaveNode node;										// 网络通信Slave节点
	ConcurrentHashMap<String, JobGroupExecutor> jobGroupExectuorMap;	// 任务分组到任务执行单元的映射
	private BlockingQueue<JobExecutionInfo> doneJobQueue;				// 已经完成的任务缓存, 有待发送到master
	private volatile boolean isPullJobRun;
	private volatile boolean isPushResultRun;
	
	public DefaultSlaveScheduler(SchedulerConfig config) {
		this.config = config;
		jobGroupExectuorMap = new ConcurrentHashMap<String, JobGroupExecutor>();
		doneJobQueue = new LinkedBlockingQueue<JobExecutionInfo>(TOP_TASK_SCHEDULER_SLAVE_DONE_JOB_QUEUE_SIZE);
		isPullJobRun = true;
		isPushResultRun = true;
	}
	
	@Override
	public boolean init() {
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                 if (!(e instanceof ThreadDeath)) {
                	 logger.error("OOPS:", e);
                     System.err.print(String.format("Exception in thread: %s", t.getName()));
                     e.printStackTrace(System.err);
                 }   
            }
        });
		
		// 初始化Master通信节点
		node = new DefaultSlaveNode(_make_waverider_config_(config));
		// 添加Slave端处理Slave拉job命令处理器
		node.addCommandHandler(CommandConstants.PUSH_JOB_COMMAND, new PushJobCommandHandler(this));
		// 添加命令发生器
		node.addCommandProvider(new CommandProvider() {
			@Override
			public String getName() {
				return SLAVE_PULL_JOB_COMMAND_PROVIDER_NAME;
			}

			@Override
			public Command produce() {
				if(isPullJobRun) {
					List<PullJobRequest> requestList = _pull_job_();
					int count = requestList.size();
					if(count > 0) {
						int size = 0;
						for (PullJobRequest request : requestList) {
							size += request.getSize();
						}
						ByteBuffer buffer = ByteBuffer.allocate(size + Integer.SIZE / Byte.SIZE);
						buffer.putInt(count);
						for (PullJobRequest request : requestList) {
							request.fillByteBuffer(buffer);
						}
						buffer.flip();
	
						logger.info("Slave Send PULL_JOB_COMMAND to master");
						requestList.clear();
						return CommandFactory.createCommand(CommandConstants.PULL_JOB_COMMAND, buffer);
					}
				}
				
				return null;
			}

			@Override
			public List<Command> produce(long count) {
				List<Command> commandList = new ArrayList<Command>();
				Command command = produce();
				if(command != null) {
					commandList.add(command);
				}
				
				return commandList;
			}
		});
		
		//
		node.addCommandProvider(new CommandProvider() {
			@Override
			public String getName() {
				return SLAVE_PUSH_JOB_RESULT_COMMAND_PROVIDER_NAME;
			}

			@Override
			public Command produce() {
				if(isPushResultRun) {
					List<JobExecutionInfo> doneJobList = _push_job_result_();
					if(doneJobList.size() > 0) {
						ByteArrayOutputStream bout = null;
						DeflaterOutputStream dout = null;
						ObjectOutputStream oout = null;
						
						try {
							bout = new ByteArrayOutputStream();
							Deflater def = new Deflater(Deflater.BEST_SPEED);
							dout = new DeflaterOutputStream(bout, def);
							oout = new ObjectOutputStream(dout);
	
							oout.writeInt(doneJobList.size());
	
							if (doneJobList.size() > 0) {
								for (JobExecutionInfo jobExecutionInfo : doneJobList) {
									oout.writeObject(jobExecutionInfo);
								}
							}
	
							oout.flush();
							dout.finish();
	
							ByteBuffer buffer = ByteBuffer.wrap(bout.toByteArray());
							logger.info("Slave send PUSH_RESULT_COMMAND to master, result count:" + doneJobList.size());
							doneJobList.clear();
							return CommandFactory.createCommand(CommandConstants.PUSH_RESULT_COMMAND, buffer);
						} catch (IOException e) {
							logger.error(e);
							e.printStackTrace();
							return null;
						} finally {
							try {
								if (oout != null) {
									oout.close();
								}
	
								if (dout != null) {
									dout.close();
								}
	
								if (bout != null) {
									bout.close();
								}
							} catch (IOException ex) {
								logger.error(ex);
								ex.printStackTrace();
							}
						}
					}
				}
				
				return null;
			}

			@Override
			public List<Command> produce(long count) {
				List<Command> commandList = new ArrayList<Command>();
				Command command = produce();
				if(command != null) {
					commandList.add(command);
				}
				
				return commandList;
			}
		});
		
		if(!node.init()) {
			logger.error("Init SlaveNode failed.");
			return false;
		}
		
		// init job group executor
		/*for(JobGroupExecutor jobGroupExecutor : jobGroupExectuorMap.values()) {
			jobGroupExecutor.init();
		}*/
		return true;
	}
	
	@Override
	public boolean start() {
		boolean ret = node.start();
		//node.startCommandProvider(SLAVE_PULL_JOB_COMMAND_PROVIDER_NAME);
		// start job group executor
		/*for(JobGroupExecutor jobGroupExecutor : jobGroupExectuorMap.values()) {
			jobGroupExecutor.start();
		}*/
		return ret;
	}
	
	@Override
	public boolean stop() {
		// stop job group executor
		for(JobGroupExecutor jobGroupExecutor : jobGroupExectuorMap.values()) {
			jobGroupExecutor.shutdown();
		}
		return node.stop();
	}
	
	@Override
	public boolean restart() {
		return node.restart();
	}
	
	@Override
	public void addJobConsumer(final String group, JobConsumer jobConsumer, ResourceConfig rconfig) {
		logger.warn(new StringBuilder("Add one jobConsumer for group:").append(group));
		JobGroupExecutor old = jobGroupExectuorMap.put(group, new ThreadPoolJobGroupExecutor(group, jobConsumer, rconfig));
		if(old != null) {
			old.shutdownNow();
		}
	}
	
	@Override
	public void handle(Job job) {
		JobExecutionInfo jobExecutionInfo = new JobExecutionInfo();
		jobExecutionInfo.setJobId(job.getId());
		jobExecutionInfo.setSlaveReceivedTime(new Date());
		job.setJobExecutionInfo(jobExecutionInfo);
		JobGroupExecutor executor = jobGroupExectuorMap.get(job.getKey().getGroup());
		executor.doJob(job, this);
		logger.info("Submit one job for group:" + executor.getGroup());
	}
	
	@Override
	public void report(boolean success, com.ihome.top.scheduler.job.Job job, JobResult result) {
		
	}

	@Override
	public void report(JobExecutionInfo jobExecutionInfo) {
		try {
			doneJobQueue.put(jobExecutionInfo);
		} catch (InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 构建WaveriderConfig, 传递给waverider
	 * @param config
	 * @return
	 */
	private WaveriderConfig _make_waverider_config_(SchedulerConfig config) {
		WaveriderConfig wconf = new WaveriderConfig();
		wconf.setPort(config.getPort());
		wconf.setMasterAddress(config.getMasterAddress());
		return wconf;
	}

	/**
	 * 参数拉取任务请求
	 * @return
	 */
	private List<PullJobRequest> _pull_job_() {
		
		List<PullJobRequest> requestList = new LinkedList<PullJobRequest>();
		JobGroupExecutor executor = null;
		Iterator<String> iterator = jobGroupExectuorMap.keySet().iterator();
		while(iterator.hasNext()) {
			String group = iterator.next();
			if(null != group) {
				executor = jobGroupExectuorMap.get(group);
				if(executor != null) {
					if(executor.remainingCapacity() > 0) {
						requestList.add(new PullJobRequest(group, executor.remainingCapacity()));
					}
				}
			}
		}
		
		return requestList;
	}
	
	/**
	 * 推送任务执行结果
	 * @return
	 */
	private List<JobExecutionInfo> _push_job_result_() {
		
		List<JobExecutionInfo> doneJobList = new LinkedList<JobExecutionInfo>();
		if(doneJobQueue.size() > 0) {
			doneJobQueue.drainTo(doneJobList);
		}
		
		return doneJobList;
	}
}
