package com.mpnet.exceptions;

/**
 * 
 * @ClassName: MPCodecException 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 下午2:45:48 
 *
 */
public class MPCodecException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3519254636935062061L;

	public MPCodecException() {
	}

	public MPCodecException(String message) {
		super(message);
	}

	public MPCodecException(Throwable t) {
		super(t);
	}
}