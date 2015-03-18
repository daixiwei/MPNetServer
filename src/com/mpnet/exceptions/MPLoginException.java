package com.mpnet.exceptions;

public class MPLoginException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8266753022225545223L;

	public MPLoginException() {
	}

	public MPLoginException(String message) {
		super(message);
	}

	public MPLoginException(String message, MPErrorData data) {
		super(message, data);
	}
}