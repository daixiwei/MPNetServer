package com.mpnet.api;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.service.IService;

/**
 * 
 * @ClassName: APIManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 下午3:03:54
 *
 */
public class APIManager implements IService {
	private final String	serviceName	= "APIManager";
	private MPNetServer		mpnet;
	private IMPApi			mpApi;
	
	public void init(Object o) {
		mpnet = MPNetServer.getInstance();
		mpApi = new MPApi(mpnet);
	}
	
	public IMPApi getApi() {
		return this.mpApi;
	}
	
	public void destroy(Object arg0) {}
	
	public String getName() {
		return serviceName;
	}
	
	public void handleMessage(Object msg) {
		throw new UnsupportedOperationException("Not supported");
	}
	
	public void setName(String arg0) {
		throw new UnsupportedOperationException("Not supported");
	}
}