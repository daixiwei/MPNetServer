package com.mpnet.exceptions;

public class MPFloodingException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6331657649310591877L;

	public MPFloodingException() {
	}

	public MPFloodingException(String message) {
		super(message);
	}

	public MPFloodingException(String message, MPErrorData data) {
		super(message, data);
	}
}