package com.ihome.top.scheduler.log;

import org.junit.Test;

import com.ihome.top.scheduler.internal.log.Logger;

/**
 * 
 * @author sihai
 *
 */
public class LoggerTest {
	
	private static int TEST_THREAD_NR = 2;
	@Test
	public void testLogger() throws Exception
	{
		Thread[] threads = new Thread[TEST_THREAD_NR];
		Logger logger = new Logger("c:\\log\\", "log.log", 16, 0.8D);
		logger.init();
		
		for(int i = 0; i < TEST_THREAD_NR; i++)
		{
			threads[i] = new Thread(new TestTask(logger), "Test-Thread-" + i);
			threads[i].start();
		}
		
		try
		{
			Thread.sleep(60000000);
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		
		logger.shutdown();
		
		for(Thread t : threads)
		{
			t.interrupt();
		}
	}
	
	private class TestTask implements Runnable
	{
		private Logger logger;
		
		public TestTask(Logger logger)
		{
			this.logger = logger;
		}
		
		@Override
		public void run() 
		{
			int i = 0;
			
			while(true)
			{
				if(Thread.currentThread().isInterrupted())
				{
					break;
				}
				
				logger.log((Thread.currentThread().getName() + " data " + i++ + "\n").getBytes());
				
				/*try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}*/
			}
			
			System.out.println(Thread.currentThread().getName() + " stoped");
		}
		
	}
}
