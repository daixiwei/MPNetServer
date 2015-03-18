package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.data.IPacket;

/**
 * 
 * @ClassName: IProtocolCodec
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:39:51
 *
 */
public interface IProtocolCodec {
	
	/**
	 * The read packet data method.
	 * 
	 * @param packet
	 */
	public void onPacketRead(IPacket packet);
	
	/**
	 * The write packet data method.
	 * 
	 * @param response
	 */
	public void onPacketWrite(IResponse response);
	
	/**
	 * The get io hander method.
	 * 
	 * @return
	 */
	public IOHandler getIOHandler();
	
	/**
	 * The set io hander method.
	 * 
	 * @param handler
	 */
	public void setIOHandler(IOHandler handler);
}