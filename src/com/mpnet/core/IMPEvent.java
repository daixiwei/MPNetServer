package com.mpnet.core;

/**
 * 
 * @ClassName: IMPEvent 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:21:15 
 *
 */
public interface IMPEvent {
	public MPEventType getType();

	public Object getParameter(IMPEventParam param);
}