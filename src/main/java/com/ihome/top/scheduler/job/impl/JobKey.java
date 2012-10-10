/**
 * top-task-scheduler
 * 
 */

package com.ihome.top.scheduler.job.impl;

import com.ihome.top.scheduler.common.Key;

/**
 * <p>
 * 任务标示
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */

public final class JobKey extends Key<JobKey> {
	
	private static final long serialVersionUID = 8551164375374294171L;

	public JobKey(String name) {
        super(name, null);
    }

    public JobKey(String name, String group) {
        super(name, group);
    }

    public static JobKey jobKey(String name) {
        return new JobKey(name, null);
    }
    
    public static JobKey jobKey(String name, String group) {
        return new JobKey(name, group);
    }
}
