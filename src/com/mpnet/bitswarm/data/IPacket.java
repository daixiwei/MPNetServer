package com.mpnet.bitswarm.data;

import com.mpnet.bitswarm.sessions.ISession;
import java.util.Collection;

/**
 * 
 * @ClassName: IPacket
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:45
 *
 */
public interface IPacket {
	public Object getData();
	
	public void setData(Object data);
	
	public TransportType getTransportType();
	
	public void setTransportType(TransportType transportType);
	
	public MessagePriority getPriority();
	
	public void setPriority(MessagePriority priority);
	
	public Collection<ISession> getRecipients();
	
	public void setRecipients(Collection<ISession> recipients);
	
	public byte[] getFragmentBuffer();
	
	public void setFragmentBuffer(byte[] bb);
	
	public ISession getSender();
	
	public void setSender(ISession sender);
	
	public Object getAttribute(String key);
	
	public void setAttribute(String key, Object value);
	
	// public String getOwnerNode();
	//
	// public void setOwnerNode(String paramString);
	
	// public long getCreationTime();
	//
	// public void setCreationTime(long creationTime);
	
	public int getOriginalSize();
	
	public void setOriginalSize(int originalSize);
	
	public boolean isTcp();
	
	public boolean isUdp();
	
	public boolean isFragmented();
	
	public IPacket clone();
}