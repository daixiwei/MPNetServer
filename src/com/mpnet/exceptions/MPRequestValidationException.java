package com.mpnet.exceptions;

public class MPRequestValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7251842733468569338L;

	public MPRequestValidationException() {
	}

	public MPRequestValidationException(String message) {
		super(message);
	}

	public MPRequestValidationException(Throwable t) {
		super(t);
	}
}