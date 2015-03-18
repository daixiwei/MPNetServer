package com.mpnet.exceptions;

/**
 * 
 * @ClassName: MessageQueueFullException 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 下午2:45:43 
 *
 */
public class MessageQueueFullException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MessageQueueFullException() {
	}

	public MessageQueueFullException(String message) {
		super(message);
	}
}