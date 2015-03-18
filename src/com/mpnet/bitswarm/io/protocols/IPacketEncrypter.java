package com.mpnet.bitswarm.io.protocols;

import com.mpnet.bitswarm.sessions.ISession;

/**
 * 
 * @ClassName: IPacketEncrypter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:28:19
 *
 */
public interface IPacketEncrypter {
	
	/**
	 * 
	 * @param session
	 * @param data
	 * @return
	 */
	public byte[] encrypt(ISession session, byte[] data);
	
	/**
	 * 
	 * @param session
	 * @param data
	 * @return
	 */
	public byte[] decrypt(ISession session, byte[] data);
}