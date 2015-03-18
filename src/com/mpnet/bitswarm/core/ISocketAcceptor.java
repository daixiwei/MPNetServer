package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.data.BindableSocket;
import com.mpnet.config.ServerSettings.SocketAddress;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @ClassName: ISocketAcceptor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午2:42:24
 *
 */
public interface ISocketAcceptor {
	/**
	 * 
	 * @param address
	 * @throws IOException
	 */
	public void bindSocket(SocketAddress address) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	public List<BindableSocket> getBoundSockets();
	
	/**
	 * 
	 */
	public void handleAcceptableConnections();
	
	/**
	 * 
	 * @return
	 */
	public IConnectionFilter getConnectionFilter();
	
	/**
	 * 
	 * @param filter
	 */
	public void setConnectionFilter(IConnectionFilter filter);
}