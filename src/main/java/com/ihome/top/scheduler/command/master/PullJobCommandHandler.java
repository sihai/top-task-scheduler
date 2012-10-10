/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.command.master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.top.scheduler.PullJobRequest;
import com.ihome.top.scheduler.command.CommandConstants;
import com.ihome.top.scheduler.internal.MasterScheduler;
import com.ihome.top.scheduler.job.internal.Job;
import com.ihome.top.waverider.command.Command;
import com.ihome.top.waverider.command.CommandFactory;
import com.ihome.top.waverider.command.CommandHandler;

/**
 * <p>
 * Master处理Slave拉取job
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class PullJobCommandHandler implements CommandHandler {
	
	private final static Log logger = LogFactory.getLog(PullJobCommandHandler.class);
	
	private MasterScheduler scheduler; 			// 

	public PullJobCommandHandler(MasterScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public Command handle(Command command) {
		//logger.info("maser.PullJobCommandHandler");
		ByteBuffer buf = command.getPayLoad();
		if (buf.remaining() > 0) {
			int requestCount = buf.getInt();
			List<PullJobRequest> requestList = new LinkedList<PullJobRequest>();
			//logger.info(new StringBuilder("Slave send ").append(requestCount).append(" pull job request.").toString());
			for (int i = 0; i < requestCount; i++) {
				PullJobRequest request = PullJobRequest.unmarshall(buf);
				logger.info(String.format("Slave:%s pull job: group=%s,max=%d", command.getSession().getSlaveWorker(), request.getGroup(), request.getMax()));
				requestList.add(request);
			}
			
			// 请求job
			Collection<Job> jobList = scheduler.dispatch(requestList, 10000, command.getSession().getSlaveWorker());
			
			// 发送job
			ByteArrayOutputStream bout = null;
			DeflaterOutputStream deflaterOutputStream = null;
			ObjectOutputStream objOutputStream = null;
			try {
				bout = new ByteArrayOutputStream();
				Deflater def = new Deflater(Deflater.BEST_SPEED);
				deflaterOutputStream = new DeflaterOutputStream(bout, def);
				objOutputStream = new ObjectOutputStream(deflaterOutputStream);
				objOutputStream.writeInt(scheduler.getUndoJobCount());
				objOutputStream.writeInt(scheduler.getDoneJobCount());
				objOutputStream.writeInt(jobList.size());
				
				if (jobList.size() > 0) {
					for (Job job : jobList) {
						objOutputStream.writeObject(job);
					}
				}

				objOutputStream.flush();
				deflaterOutputStream.finish();

				ByteBuffer buffer = ByteBuffer.wrap(bout.toByteArray());
				logger.info(new StringBuilder("Master send ").append(jobList.size()).append(" jobs to slave:").append(command.getSession().getSlaveWorker()));
				jobList.clear();
				return CommandFactory.createCommand(CommandConstants.PUSH_JOB_COMMAND, buffer);
			} catch (IOException e) {
				logger.error("OOPS: Exception:", e);
				e.printStackTrace();
			} finally {
				try {
					if (objOutputStream != null) {
						objOutputStream.close();
					}

					if (deflaterOutputStream != null) {
						deflaterOutputStream.close();
					}

					if (bout != null) {
						bout.close();
					}
				} catch (IOException ex) {
					logger.error(ex);
					ex.printStackTrace();
				}
			}
		} else {
			logger.warn("Slave send wrong request");
		}
		
		return null;
	}
}
