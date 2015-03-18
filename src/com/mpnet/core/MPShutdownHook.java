package com.mpnet.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpnet.MPNetServer;
import com.mpnet.db.IDataMappManager;

/**
 * 
 * @ClassName: MPShutdownHook 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:22:23 
 *
 */
public class MPShutdownHook extends Thread {
	private final Logger log;

	public MPShutdownHook() {
		super("MPNet ShutdownHook");
		log = LoggerFactory.getLogger(getClass());
	}

	public void run() {
		try {
			IDataMappManager dataMappManager = MPNetServer.getInstance().getDBManager().getDataMappManager();
			if(dataMappManager!=null){
				dataMappManager.enforceCommit();
				log.info("commit data base!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}