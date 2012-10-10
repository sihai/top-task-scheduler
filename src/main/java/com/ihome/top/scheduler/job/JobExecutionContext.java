package com.ihome.top.scheduler.job;

import java.io.Serializable;

/**
 * 具体的Job的结果必须实现本接口，其实就是为序列化，保证在网络上传输
 * 
 * @author raoqiang
 *
 */
public interface JobExecutionContext extends Serializable {

}
