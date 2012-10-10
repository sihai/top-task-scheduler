package com.ihome.top.scheduler.common;

/**
 * <p>
 * �ڵ�����ģʽ
 * </p>
 * 
 * <p>
 * <b>֧�ֵ�ģʽ:</b>
 * <ul>
 * <li>Master, �ڵ�������Masterģʽ, ֻ�����������͵�������, ��ִ������</li>
 * <li>Slave,  �ڵ�������Slaveģʽ, ֻ����ִ������, ������Master��ȡ����Ȼ��ִ��</li>
 * </ul>
 * </p>
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public enum RuningModeEnum {
	
	RUNING_MODE_MASTER(0, "Run as master"),
	RUNING_MODE_SLAVE(1, "Run as slave");
	
	private final int value;		// ֵ
	private final String desc;		// ����
	
	public int getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	private RuningModeEnum(int value, String desc) {
		 this.value = value;
		 this.desc = desc;
	}
	
	public static RuningModeEnum toEnum(int value) {
		for(RuningModeEnum e : RuningModeEnum.values()) {
			if(e.value == value) {
				return e;
			}
		}
		
		return null;
	}
}
