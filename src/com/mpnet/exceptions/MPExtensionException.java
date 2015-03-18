package com.mpnet.exceptions;

public class MPExtensionException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3386877765953045457L;

	public MPExtensionException() {
	}

	public MPExtensionException(String message) {
		super(message);
	}

	public MPExtensionException(String message, MPErrorData data) {
		super(message, data);
	}
}