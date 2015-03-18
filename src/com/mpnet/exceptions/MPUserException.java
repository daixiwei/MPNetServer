package com.mpnet.exceptions;

public class MPUserException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7405420328408592740L;

	public MPUserException() {
	}

	public MPUserException(String message) {
		super(message);
	}

	public MPUserException(String message, MPErrorData data) {
		super(message, data);
	}
}