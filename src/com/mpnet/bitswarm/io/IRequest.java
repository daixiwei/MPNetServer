package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.data.MessagePriority;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.sessions.ISession;

/**
 * 
 * @ClassName: IRequest
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:54:00
 *
 */
public interface IRequest extends IEngineMessage {
	public TransportType getTransportType();
	
	public void setTransportType(TransportType tType);
	
	public ISession getSender();
	
	public void setSender(ISession session);
	
	public MessagePriority getPriority();
	
	public void setPriority(MessagePriority priority);
	
	public long getTimeStamp();
	
	public void setTimeStamp(long timeStamp);
	
	public boolean isTcp();
	
	public boolean isUdp();
}