package com.mpnet.bitswarm.core;

import com.mpnet.exceptions.RefusedAddressException;

/**
 * 
 * @ClassName: IConnectionFilter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:06:22
 *
 */
public interface IConnectionFilter {
	public void addBannedAddress(String ipAddress);
	
	public void removeBannedAddress(String ipAddress);
	
	public String[] getBannedAddresses();
	
	public void validateAndAddAddress(String ipAddress) throws RefusedAddressException;
	
	public void removeAddress(String ipAddress);
	
	public void addWhiteListAddress(String ipAddress);
	
	public void removeWhiteListAddress(String ipAddress);
	
	public String[] getWhiteListAddresses();
	
	public int getMaxConnectionsPerIp();
	
	public void setMaxConnectionsPerIp(int max);
}