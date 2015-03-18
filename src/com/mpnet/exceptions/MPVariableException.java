package com.mpnet.exceptions;

public class MPVariableException extends MPException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3107764378929324818L;

	public MPVariableException() {
	}

	public MPVariableException(String message) {
		super(message);
	}

	public MPVariableException(String message, MPErrorData data) {
		super(message, data);
	}
}