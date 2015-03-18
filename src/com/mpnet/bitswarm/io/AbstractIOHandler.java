package com.mpnet.bitswarm.io;

/**
 * 
 * @ClassName: AbstractIOHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:53:44
 *
 */
public abstract class AbstractIOHandler implements IOHandler {
	protected IProtocolCodec	codec;
	protected volatile long		readPackets;
	
	public IProtocolCodec getCodec() {
		return this.codec;
	}
	
	public void setCodec(IProtocolCodec codec) {
		this.codec = codec;
	}
	
	public long getReadPackets() {
		return this.readPackets;
	}
}