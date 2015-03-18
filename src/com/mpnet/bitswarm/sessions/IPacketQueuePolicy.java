package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.exceptions.PacketQueueWarning;

/**
 * 
 * @ClassName: IPacketQueuePolicy 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:37:47 
 *
 */
public interface IPacketQueuePolicy {
	
	/**
	 * 
	 * @param queue
	 * @param packet
	 * @throws PacketQueueWarning
	 */
	public void applyPolicy(IPacketQueue queue, IPacket packet) throws PacketQueueWarning;
}