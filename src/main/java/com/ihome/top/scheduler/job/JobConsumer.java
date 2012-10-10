package com.ihome.top.scheduler.job;



/**
 * <p>
 * 处理job的接口，可以根据具体业务实现本接口
 * </p>
 * 
 * <strong>Note:</strong>
 * 		<p>目前Slave直接调用异步方法workNoBlocking，后期可以改进一下，在Job里面添加一个属性，
 * 指示是调用同步方法还是异步方法。</p>
 * 
 * @author raoqiang
 *
 */
public interface JobConsumer {
	
	/**
	 * 消费job, 同步调用, 请保证是线程安全的
	 * @param job
	 * @return
	 * @throws Exception
	 */
	JobResult work(Job job) throws Exception;
	
	/**
	 * 消费job，异步调用，job完成后，通过callback回调通知job完成
	 * @deprecated
	 * @param job
	 * @throws Exception
	 */
	void workNoBlocking(Job job, SlaveJobCompletedCallback callback) throws Exception;
}
