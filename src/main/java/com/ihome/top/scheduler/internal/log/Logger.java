/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.internal.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.common.ServiceStatusEnum;

/**
 * <p>
 * 通用日志, 按天切换日志类似log4j的DailyRollingFileAppender
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class Logger {
	
	private static final Log logger = LogFactory.getLog(Logger.class);
	
	private BlockingQueue<byte[]> queue;			// 日志缓冲
	private String logDirectory;					// 日志目录
	private String logFileName;						// 日志名称
	private int	   queueSize;						// 缓存大小
	private Double threshold;						// 刷新日志缓冲阀值 
	private int    highWaterMark;					//
	private ReentrantLock flushLock;				//
	private Condition flushCondition;				//
	private boolean isFlush;						//
	private Thread flushThread;						// 日志刷新线程
	private FileOutputStream out;					// 
	private volatile ServiceStatusEnum _status_;	// 日志状态
	private Date lastSwitchLogTime;					// 上一次切换日志时间
	
	public Logger(String logDirectory, String logFileName, int queueSize, Double threshold) {
		_status_ = ServiceStatusEnum.SERVICE_SHUTDOWN;
		this.logDirectory = logDirectory;
		this.logFileName = logFileName;
		this.queueSize = queueSize;
		this.threshold = threshold;
		this.highWaterMark = Double.valueOf(queueSize * threshold).intValue();
	}
	
	public void init() {
		logger.info("Logger try init");
		_status_ = ServiceStatusEnum.SERVICE_INITING;
		lastSwitchLogTime = new Date();
		flushLock = new ReentrantLock();
		flushCondition = flushLock.newCondition();
		isFlush = false;
		
		// 打开日志文件
		_openLog();
		
		queue = new LinkedBlockingQueue<byte[]>(queueSize);
		flushThread = new Thread(new LoggerFlushTask(), "Top-Task-Scheduler-Logger-Flush-Thread");
		flushThread.setDaemon(true);
		flushThread.start();
		
		_status_ = ServiceStatusEnum.SERVICE_SERVICEING;
		logger.info("Logger init ok, in servicing");
	}
	
	// 
	public void log(byte[] data) {
		// FIXME
		//logger.info(new StringBuilder("[Logger] content:").append(data).toString());
		if(_status_ != ServiceStatusEnum.SERVICE_SERVICEING) {
			throw new RuntimeException("Logger not in servicing");
		}
		
		// 刷新日志缓冲区
		if(queue.size() > highWaterMark) {
			_triggerFlush();
		}
		
		try {
			// 尝试一次
			boolean ret = queue.offer(data);
			while(!ret) {
				_triggerFlush();
				ret = queue.offer(data, 100, TimeUnit.MILLISECONDS);
			}
		} catch(InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
		}
	}
	
	public void shutdown() {
		logger.info("Logger try to shutdown");
		_status_ = ServiceStatusEnum.SERVICE_SHUTDOWNING;
		flushThread.interrupt();
	}
	
	
	//====================================================
	//		内部私有方法
	//====================================================
	private void _clean() throws IOException {
		_flush();
		out.close();
		queue.clear();
	}
	
	private void _triggerFlush() {
		try {
			flushLock.lock();
			isFlush = true;
			flushCondition.signalAll();
		} finally {
			flushLock.unlock();
		}
	}
	
	/**
	 * 刷新日志缓冲
	 * @throws IOException
	 */
	private void _flush() throws IOException {
		int size = 0;
		logger.info("Logger try to flush");
		List<byte[]> buf = new LinkedList<byte[]>();
		queue.drainTo(buf);
		
		for(byte[] data : buf) {
			out.write(data);
		}
		size = buf.size();
		buf.clear();
		out.flush();
		logger.info("Logger flush end, flushed " + size + " records");
	}
	
	// 打开日志文件
	private void _openLog() {
		try {
			out = new FileOutputStream(new File(getLogFileName()), true);
		}
		catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	// 尝试切换日志文件
	private void _trySwitchLog() throws IOException {
		Calendar last = Calendar.getInstance();
		last.setTime(lastSwitchLogTime);
		
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		
		if(last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
			return;
		}
		
		// switch log
		out.flush();
		out.close();
		
		_rename();
		_openLog();
		lastSwitchLogTime = new Date();
	}
	
	// 对日志文件重命名
	private void _rename() {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - 1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		File file = new File(getLogFileName());
		File target  = new File(new StringBuilder(getLogFileName()).append(".").append(format.format(now.getTime())).toString());
	    if (target.exists()) {
	      target.delete();
	    }
		if(file.renameTo(target)) {
			logger.info("Logger switch log file succeed");
		} else {
			logger.info("Logger switch log file failed");
		}
	}
	
	// 构造文件名
	private String getLogFileName() {
		StringBuilder sb = new StringBuilder();
		sb.append(logDirectory);
		if(sb.charAt(sb.length() - 1) != File.separatorChar) {
			sb.append(File.separatorChar);
		}
		
		sb.append(logFileName);
		return sb.toString();
	}
	
	// 后台日志刷新线程
	private class LoggerFlushTask implements Runnable {	
		@Override
		public void run() {
			try {
				while(!Thread.currentThread().isInterrupted()) {
					try {
						flushLock.lock();
						while(!isFlush) {
							flushCondition.await();
						}
						_flush();
						_trySwitchLog();
						isFlush = false;
					} finally {
						flushLock.unlock();
					}
				}
				
				// 循环退出
				_clean();
				_status_ = ServiceStatusEnum.SERVICE_SHUTDOWN;
				logger.info("Logger shutdown ok, in Shutdown");
			} catch(InterruptedException e) {
				logger.error(e);
				Thread.currentThread().interrupt();
			} catch(IOException e) {
				logger.error(e);
				_status_ = ServiceStatusEnum.SERVICE_SHUTDOWN;
			}
		}	
	}
}
