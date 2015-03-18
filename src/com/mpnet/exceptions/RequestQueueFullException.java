package com.mpnet.exceptions;

public class RequestQueueFullException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequestQueueFullException() {
	}

	public RequestQueueFullException(String message) {
		super(message);
	}
}