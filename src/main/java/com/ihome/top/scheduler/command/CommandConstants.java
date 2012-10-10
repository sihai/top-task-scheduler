/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.command;

/**
 * 
 * @author sihai
 *
 */
public interface CommandConstants {

	Long PULL_JOB_COMMAND = 10L;			// 拉任务命令Type
	Long PUSH_RESULT_COMMAND = 11L;			// 推送任务结果命令Type
	Long PUSH_JOB_COMMAND = 12L;			// 
	Long CANCEL_DOING_JOB_COMMAND = 13L;	// 取消执行中的任务
	Long RELOAD_JOB_REQUEST_COMMAND = 14L;	// Master 请求Slave Reload, Slave将完成的任务立马发送给Master, 取消进行中的Job
	Long RELOAD_JOB_READY_COMMAND = 15L;    // Slave完成Reload清理工作后, 发送本命令通知Master
	Long RELOAD_JOB_COMPLETED_COMMAND = 16L;// Master完成Reload, 通知Slave可以拉任务了
}
