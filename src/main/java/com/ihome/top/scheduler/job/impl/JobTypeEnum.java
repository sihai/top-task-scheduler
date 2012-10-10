package com.ihome.top.scheduler.job.impl;

/**
 * 
 * Job类型, 定义Job的执行类型
 * 
 * @author sihai
 *
 */
public enum JobTypeEnum 
{
	JOB_TYPE_EXEC_ONCE(0, "Execute once"),
	JOB_TYPE_EXEC_PERIOD(1,"Execute period");
	
	int value;
	String desc;
	
	private JobTypeEnum(int value, String desc)
	{
		this.value = value;
		this.desc = desc;
	}
	
	public JobTypeEnum toEnum(int value)
	{
		for(JobTypeEnum e : this.values())
		{
			if(e.value == value)
			{
				return e;
			}
		}
		
		return null;
	}
}
