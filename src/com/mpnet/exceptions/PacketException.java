package com.mpnet.exceptions;

public class PacketException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final byte[] originalPacket;
	private final byte[] writtenPacket;

	public PacketException() {
		this.originalPacket = null;
		this.writtenPacket = null;
	}

	public PacketException(String message) {
		this(message, null, null);
	}

	public PacketException(String message, byte[] original, byte[] written) {
		super(message);
		this.originalPacket = original;
		this.writtenPacket = written;
	}

	public byte[] getOriginalPacket() {
		return this.originalPacket;
	}

	public byte[] getWrittenPacket() {
		return this.writtenPacket;
	}
}