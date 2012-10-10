/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.command.master;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.internal.MasterScheduler;
import com.ihome.top.scheduler.job.impl.JobExecutionInfo;
import com.ihome.top.waverider.command.Command;
import com.ihome.top.waverider.command.CommandHandler;
import com.ihome.top.waverider.network.Packet;

/**
 * <p>
 * Master处理Slave推送任务执行结果
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class PushResultCommandHandler implements CommandHandler {

	private final static Log logger = LogFactory.getLog(PullJobCommandHandler.class);

	private MasterScheduler scheduler; 			// 

	public PushResultCommandHandler(MasterScheduler scheduler) {
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

				int jobCount = objInputStream.readInt();
				logger.info(new StringBuilder("The slave:").append(command.getSession().getSlaveWorker()).append(" send ").append(jobCount).append(" result"));
				for (int i = 0; i < jobCount; i++) {
					JobExecutionInfo jobExecutionInfo = (JobExecutionInfo) objInputStream.readObject();
					scheduler.completed(jobExecutionInfo);
				}
			} catch (IOException e) {
				logger.error(e);
			} catch (ClassNotFoundException e) {
				logger.error(e);
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
				}
			}
		} else {
			logger.warn("Slave send wrong result");
			return null;
		}
		
		return null;
	}
}
