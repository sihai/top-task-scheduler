/**
 * top-task-scheduler
 */
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

}
