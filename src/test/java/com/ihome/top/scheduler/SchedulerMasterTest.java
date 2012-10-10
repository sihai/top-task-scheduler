package com.ihome.top.scheduler;

import com.ihome.top.scheduler.impl.DefaultMasterScheduler;

public class SchedulerMasterTest extends BaseTopTaskScheduleTestCase
{
	private DefaultMasterScheduler scheduler;

	public void testMaster() throws Exception
	{
		scheduler.init();
		scheduler.start();
		
		Thread.currentThread().join();
	}
	
	public void setScheduler(DefaultMasterScheduler scheduler)
	{
		this.scheduler = scheduler;
	}
}
