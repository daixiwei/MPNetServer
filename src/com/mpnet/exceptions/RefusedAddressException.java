package com.mpnet.exceptions;

public class RefusedAddressException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RefusedAddressException() {
	}

	public RefusedAddressException(String message) {
		super(message);
	}
}