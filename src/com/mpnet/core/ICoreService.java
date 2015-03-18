package com.mpnet.core;

import com.mpnet.bitswarm.service.IService;

/**
 * 
 * @ClassName: ICoreService 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:20:38 
 *
 */
public interface ICoreService extends IService {
	
	/**
	 * 
	 * @return
	 */
	public boolean isActive();
}