package com.mpnet.exceptions;

/**
 * 
 * @ClassName: InterruptedEventException 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 下午2:45:39 
 *
 */
public final class InterruptedEventException extends MPRuntimeException {
	private static final long serialVersionUID = 1729674312557697005L;

	public InterruptedEventException() {
		super("Event Interrupted");
	}
}