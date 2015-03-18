package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.sessions.ISession;

/**
 * 
 * @ClassName: ISocketWriter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:03:40
 *
 */
public interface ISocketWriter {
	public IOHandler getIOHandler();
	
	public void setIOHandler(IOHandler paramIOHandler);
	
	public void continueWriteOp(ISession paramISession);
	
	public void enqueuePacket(IPacket paramIPacket);
	
	public long getDroppedPacketsCount();
	
	public long getWrittenBytes();
	
	public long getWrittenPackets();
	
	public int getQueueSize();
	
	public int getThreadPoolSize();
}