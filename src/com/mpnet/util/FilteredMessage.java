package com.mpnet.util;

/**
 * 
 * @ClassName: FilteredMessage
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午5:32:36
 *
 */
public class FilteredMessage {
	private String	message;
	private int		occurrences;
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public int getOccurrences() {
		return this.occurrences;
	}
	
	public void setOccurrences(int substitutions) {
		this.occurrences = substitutions;
	}
}