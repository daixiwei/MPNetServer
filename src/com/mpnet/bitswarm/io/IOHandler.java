package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.sessions.ISession;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * 
 * @ClassName: IOHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:53:14
 *
 */
public interface IOHandler {
	public void onDataRead(ISession session, byte[] data);
	
	public void onDataRead(DatagramChannel channel, SocketAddress address, byte[] data);
	
	public void onDataWrite(IPacket packet);
	
	public IProtocolCodec getCodec();
	
	public void setCodec(IProtocolCodec codec);
	
	public long getReadPackets();
	
	public long getIncomingDroppedPackets();
}