package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.io.IOHandler;
import java.nio.channels.Selector;

/**
 * 
 * @ClassName: ISocketReader
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午2:42:34
 *
 */
public interface ISocketReader {
	
	/**
	 * 
	 * @return
	 */
	public Selector getSelector();
	
	/**
	 * 
	 * @return
	 */
	public IOHandler getIOHandler();
	
	/**
	 * 
	 * @param iohandler
	 */
	public void setIoHandler(IOHandler iohandler);
	
	/**
	 * 
	 * @return
	 */
	public long getReadBytes();
	
	/**
	 * 
	 * @return
	 */
	public long getReadPackets();
}