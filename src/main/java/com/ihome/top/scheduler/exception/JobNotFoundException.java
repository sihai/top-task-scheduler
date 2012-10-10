package com.ihome.top.scheduler.exception;
/**
 * JobNotFoundException
 * @author yunzhan.jtq
 * 
 * @since 2011-5-23 上午10:30:45
 */
public class JobNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 313703952444656665L;
	
	
	public JobNotFoundException(Throwable t)
	{
		super(t);
	}
	
	public JobNotFoundException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public JobNotFoundException(String msg)
	{
		super(msg);
	}
}