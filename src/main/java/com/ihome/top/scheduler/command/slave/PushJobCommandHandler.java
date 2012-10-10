/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.command.slave;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.internal.SlaveScheduler;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.waverider.command.Command;
import com.ihome.top.waverider.command.CommandHandler;
import com.ihome.top.waverider.network.Packet;

/**
 * <p>
 * Slave处理Master推送任务命令处理器
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class PushJobCommandHandler implements CommandHandler {
	
	private final static Log logger = LogFactory.getLog(PushJobCommandHandler.class);

	private SlaveScheduler scheduler;

	public PushJobCommandHandler(SlaveScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public Command handle(Command command) {
		
		ByteBuffer buffer = command.getPayLoad();

		if (buffer.remaining() > 0) {
			ByteArrayInputStream bin = null;
			InflaterInputStream inflaterInputStream = null;
			ObjectInputStream objInputStream = null;

			try {
				bin = new ByteArrayInputStream(buffer.array(), Packet.getHeaderSize() + Command.getHeaderSize(), buffer.remaining());
				inflaterInputStream = new InflaterInputStream(bin);
				objInputStream = new ObjectInputStream(inflaterInputStream);

				int undoJobCount = objInputStream.readInt();
				int doneJobCount = objInputStream.readInt();
				int jobCount = objInputStream.readInt();

				logger.info(new StringBuilder("Master send ").append(jobCount)
						.append(" jobs, master has: undoJobCount=").append(
								undoJobCount).append(", doneJobCount=").append(
								doneJobCount));

				for (int i = 0; i < jobCount; i++) {
					Job job = (Job) objInputStream.readObject();
					scheduler.handle(job);
				}
				
				return null;
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				logger.error(e);
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				return null;
			} finally {
				try {
					if (objInputStream != null)
						objInputStream.close();

					if (inflaterInputStream != null)
						inflaterInputStream.close();

					if (bin != null)
						bin.close();

					objInputStream = null;
					inflaterInputStream = null;
					bin = null;
				} catch (IOException ex) {
					logger.error(ex);
					ex.printStackTrace();
				}
			}
		} else {
			logger.warn("Master send wrong result");
			return null;
		}
	}
}
