package com.mpnet.core;

import java.util.Map;

/**
 * 
 * @ClassName: MPSystemEvent 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:22:28 
 *
 */
public class MPSystemEvent extends MPEvent {
	private final Map<IMPEventParam, Object> sysParams;

	public MPSystemEvent(MPEventType type, Map<IMPEventParam, Object> params, Map<IMPEventParam, Object> sysParams) {
		super(type, params);
		this.sysParams = sysParams;
	}

	public Object getSysParameter(IMPEventParam key) {
		return this.sysParams.get(key);
	}

	public void setSysParameter(IMPEventParam key, Object value) {
		if (this.sysParams != null)
			this.sysParams.put(key, value);
	}
}