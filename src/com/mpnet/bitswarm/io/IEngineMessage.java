package com.mpnet.bitswarm.io;

/**
 * 
 * @ClassName: IEngineMessage
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:28:44
 *
 */
public interface IEngineMessage {
	public Object getId();
	
	public void setId(Object id);
	
	public Object getContent();
	
	public void setContent(Object content);
	
	public Object getAttribute(String key);
	
	public void setAttribute(String key, Object value);
}