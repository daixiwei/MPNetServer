package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.exceptions.MessageQueueFullException;

/**
 * 
 * @ClassName: IPacketQueue 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:26:07 
 *
 */
public interface IPacketQueue {
	public IPacket peek();

	public IPacket take();

	public boolean isEmpty();

	public boolean isFull();

	public int getSize();

	public int getMaxSize();

	public void setMaxSize(int size);

	public float getPercentageUsed();

	public void clear();

	public void put(IPacket packet) throws MessageQueueFullException;

	public IPacketQueuePolicy getPacketQueuePolicy();

	public void setPacketQueuePolicy(IPacketQueuePolicy policy);
}