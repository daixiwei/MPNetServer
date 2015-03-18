package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.sessions.ISession;
import java.util.Collection;

/**
 * 
 * @ClassName: IResponse
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:28:11
 *
 */
public interface IResponse extends IEngineMessage {
	public TransportType getTransportType();
	
	public void setTransportType(TransportType type);
	
	public Object getTargetController();
	
	public void setTargetController(Object o);
	
	public Collection<ISession> getRecipients();
	
	public void setRecipients(Collection<ISession> recipents);
	
	public void setRecipients(ISession session);
	
	public boolean isTCP();
	
	public boolean isUDP();
	
	public void write();
	
	public void write(int delay);
}