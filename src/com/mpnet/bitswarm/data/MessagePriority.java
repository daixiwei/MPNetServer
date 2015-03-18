package com.mpnet.bitswarm.data;

/**
 * 
 * @ClassName: MessagePriority 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:59:58 
 *
 */
public enum MessagePriority {
	VERY_LOW(1, "Very LOW"), 
	LOW(2, "LOW"), 
	NORMAL(3, "NORMAL"), 
	HIGH(4, "HIGH"), 
	VERY_HIGH(5, "Very HIGH");
	private int		level;
	private String	repr;
	
	private MessagePriority(int lev, String repr) {
		this.level = lev;
		this.repr = repr;
	}
	
	public int getValue() {
		return this.level;
	}
	
	public String toString() {
		return "{ " + this.repr + " }";
	}
}