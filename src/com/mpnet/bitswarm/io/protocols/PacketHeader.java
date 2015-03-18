package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: PacketHeader
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午2:13:18
 *
 */
public final class PacketHeader {
	private int				expectedLen	= -1;
	private final boolean	binary;
	private final boolean	compressed;
	private final boolean	encrypted;
	private final boolean	blueBoxed;
	private final boolean	bigSized;
	
	public PacketHeader(boolean binary, boolean encrypted, boolean compressed, boolean blueBoxed, boolean bigSized) {
		this.binary = binary;
		this.compressed = compressed;
		this.encrypted = encrypted;
		this.blueBoxed = blueBoxed;
		this.bigSized = bigSized;
	}
	
	public int getExpectedLen() {
		return this.expectedLen;
	}
	
	public void setExpectedLen(int len) {
		this.expectedLen = len;
	}
	
	public boolean isBinary() {
		return this.binary;
	}
	
	public boolean isCompressed() {
		return this.compressed;
	}
	
	public boolean isEncrypted() {
		return this.encrypted;
	}
	
	public boolean isBlueBoxed() {
		return this.blueBoxed;
	}
	
	public boolean isBigSized() {
		return this.bigSized;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("\n---------------------------------------------\n");
		buf.append("Binary:  \t" + isBinary() + "\n");
		buf.append("Compressed:\t" + isCompressed() + "\n");
		buf.append("Encrypted:\t" + isEncrypted() + "\n");
		buf.append("BlueBoxed:\t" + isBlueBoxed() + "\n");
		buf.append("BigSized:\t" + isBigSized() + "\n");
		buf.append("---------------------------------------------\n");
		
		return buf.toString();
	}
}