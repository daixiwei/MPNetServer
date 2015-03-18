package com.mpnet.extensions;

import com.mpnet.bitswarm.service.ISimpleService;

/**   
 * @author daixiwei daixiwei15@126.com 
 * @date 2015年3月7日 下午5:05:36 
 * @version V2.9   
 */
public abstract class BaseExtensionService implements ISimpleService{
	protected MPExtension extension;
	
	/**
	 * 
	 * @param extension
	 */
	void setExtension(MPExtension extension){
		this.extension = extension;
	}
	
	/**
	 * The trace extension log
	 * @param args
	 */
	public void trace(Object... args) {
		trace(ExtensionLogLevel.INFO, args);
	}

	/**
	 * The trace extension log
	 * @param level		
	 * @param args
	 */
	public void trace(ExtensionLogLevel level, Object... args) {
		extension.trace(level, args);
	}
}
