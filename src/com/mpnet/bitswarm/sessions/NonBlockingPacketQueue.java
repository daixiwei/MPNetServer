package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.exceptions.MessageQueueFullException;

import java.util.LinkedList;

/**
 * 
 * @ClassName: NonBlockingPacketQueue 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:23:49 
 *
 */
public final class NonBlockingPacketQueue implements IPacketQueue {
	private final LinkedList<IPacket> queue;
	private int maxSize;
	private IPacketQueuePolicy packetQueuePolicy;

	public NonBlockingPacketQueue(int maxSize) {
		this.queue = new LinkedList<IPacket>();
		this.maxSize = maxSize;
	}

	public void clear() {
		synchronized (this.queue) {
			this.queue.clear();
		}
	}

	public int getSize() {
		return this.queue.size();
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public boolean isEmpty() {
		return this.queue.size() == 0;
	}

	public boolean isFull() {
		return this.queue.size() >= this.maxSize;
	}

	public float getPercentageUsed() {
		if (this.maxSize == 0) {
			return 0.0F;
		}
		return this.queue.size() * 100 / this.maxSize;
	}

	public IPacket peek() {
		IPacket packet = null;

		synchronized (this.queue) {
			if (!isEmpty()) {
				packet = (IPacket) this.queue.get(0);
			}
		}
		return packet;
	}

	public void put(IPacket packet) throws MessageQueueFullException {
		if (isFull()) {
			throw new MessageQueueFullException();
		}

		this.packetQueuePolicy.applyPolicy(this, packet);

		synchronized (this.queue) {
			this.queue.addLast(packet);
		}
	}

	public void setMaxSize(int size) {
		this.maxSize = size;
	}

	public IPacket take() {
		IPacket packet = null;

		synchronized (this.queue) {
			packet = (IPacket) this.queue.removeFirst();
		}

		return packet;
	}

	public IPacketQueuePolicy getPacketQueuePolicy() {
		return this.packetQueuePolicy;
	}

	public void setPacketQueuePolicy(IPacketQueuePolicy packetQueuePolicy) {
		this.packetQueuePolicy = packetQueuePolicy;
	}
}