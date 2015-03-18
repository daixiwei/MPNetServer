package com.mpnet.exceptions;

/**
 * 
 * @ClassName: MPErrorCode 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 下午3:27:34 
 *
 */
public enum MPErrorCode implements IErrorCode{
	  HANDSHAKE_API_OBSOLETE(0), 
	  LOGIN_BAD_ZONENAME(1), 
	  LOGIN_BAD_USERNAME(2), 
	  LOGIN_BAD_PASSWORD(3), 
	  LOGIN_BANNED_USER(4), 
	  LOGIN_ALREADY_LOGGED(6), 
	  LOGIN_SERVER_FULL(7), 
	  LOGIN_INACTIVE_ZONE(8), 
	  LOGIN_NAME_CONTAINS_BAD_WORDS(9), 
	  LOGIN_GUEST_NOT_ALLOWED(10), 
	  LOGIN_BANNED_IP(11), 
	  GENERIC_ERROR(28);

	  private short id;
	
	  private MPErrorCode(int id) { 
		  this.id = (short)id;
	  }
	
	  public short getId(){
	    return this.id;
	  }
}