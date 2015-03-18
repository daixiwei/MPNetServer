package com.mpnet.exceptions;

public class PacketQueueWarning extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PacketQueueWarning() {
	}

	public PacketQueueWarning(String message) {
		super(message);
	}
}