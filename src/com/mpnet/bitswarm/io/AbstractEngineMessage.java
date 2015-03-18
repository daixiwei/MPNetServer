package com.mpnet.bitswarm.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @ClassName: AbstractEngineMessage
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:40:03
 *
 */
public abstract class AbstractEngineMessage implements IEngineMessage {
	protected Object				id;
	protected Object				content;
	protected Map<String, Object>	attributes;
	
	public Object getId() {
		return this.id;
	}
	
	public void setId(Object id) {
		this.id = id;
	}
	
	public Object getContent() {
		return this.content;
	}
	
	public void setContent(Object content) {
		this.content = content;
	}
	
	public Object getAttribute(String key) {
		Object attr = null;
		
		if (this.attributes != null) {
			attr = this.attributes.get(key);
		}
		return attr;
	}
	
	public void setAttribute(String key, Object attribute) {
		if (this.attributes == null) {
			this.attributes = new ConcurrentHashMap<String, Object>();
		}
		this.attributes.put(key, attribute);
	}
}