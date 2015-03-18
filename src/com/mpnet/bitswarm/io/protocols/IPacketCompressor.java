package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: IPacketCompressor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午2:11:36
 *
 */
public interface IPacketCompressor {
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] compress(byte[] data) throws Exception;
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] uncompress(byte[] data) throws Exception;
}