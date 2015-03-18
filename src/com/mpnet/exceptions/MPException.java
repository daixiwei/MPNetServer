package com.mpnet.exceptions;

public class MPException extends Exception {
	private static final long serialVersionUID = 6052949605652105170L;
	MPErrorData errorData;

	public MPException() {
		this.errorData = null;
	}

	public MPException(String message) {
		super(message);
		this.errorData = null;
	}

	public MPException(String message, MPErrorData data) {
		super(message);
		this.errorData = data;
	}

	public MPException(Throwable t) {
		super(t);
		this.errorData = null;
	}

	public MPErrorData getErrorData() {
		return this.errorData;
	}
}