package com.mpnet.exceptions;

public class MPRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4715160623281849902L;

	public MPRuntimeException() {
	}

	public MPRuntimeException(String message) {
		super(message);
	}

	public MPRuntimeException(Throwable t) {
		super(t);
	}
}