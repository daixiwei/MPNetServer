package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.data.MessagePriority;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.sessions.ISession;

/**
 * 
 * @ClassName: Request
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:56:04
 *
 */
public final class Request extends AbstractEngineMessage implements IRequest {
	private ISession		sender;
	private TransportType	type;
	private MessagePriority	priority;
	private long			timeStamp;
	
	public Request() {
		this.type = TransportType.TCP;
		this.priority = MessagePriority.NORMAL;
		this.timeStamp = System.nanoTime();
	}
	
	public ISession getSender() {
		return this.sender;
	}
	
	public TransportType getTransportType() {
		return this.type;
	}
	
	public void setSender(ISession session) {
		this.sender = session;
	}
	
	public void setTransportType(TransportType type) {
		this.type = type;
	}
	
	public MessagePriority getPriority() {
		return this.priority;
	}
	
	public void setPriority(MessagePriority priority) {
		this.priority = priority;
	}
	
	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public boolean isTcp() {
		return this.type == TransportType.TCP;
	}
	
	public boolean isUdp() {
		return this.type == TransportType.UDP;
	}
	
	public String toString() {
		return String.format("[Req Type: %s, Prt: %s, Sender: %s]", new Object[] { this.type, this.priority, this.sender });
	}
}