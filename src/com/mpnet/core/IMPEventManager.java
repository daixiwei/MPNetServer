package com.mpnet.core;

import java.util.concurrent.Executor;

import com.mpnet.bitswarm.service.IService;

/**
 * 
 * @ClassName: IMPEventManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:21:30 
 *
 */
public interface IMPEventManager extends IMPEventDispatcher, IService {
	public void setThreadPoolSize(int paramInt);

	public Executor getThreadPool();
}
