package com.ihome.top.scheduler.job;

import java.util.List;

/**
 * Job生产者接口，具体的业务可以实现本接口以提供相应的Job
 * 
 * @author raoqiang
 *
 */
public interface JobProvider {
	
	/**
	 * 产生Job
	 * @param jobManager
	 * @return
	 */
	public List<Job> generate(JobManager jobManager);

}