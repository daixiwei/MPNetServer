package com.mpnet.core;

/**
 * 
 * @ClassName: MPEventType 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:22:16 
 *
 */
public enum MPEventType {
	SERVER_READY,

//	USER_LOGIN,

	USER_JOIN_SERVER,

	USER_LOGOUT,

	USER_DISCONNECT,

	USER_RECONNECTION_TRY,

	USER_RECONNECTION_SUCCESS,

	__TRACE_MESSAGE;
}