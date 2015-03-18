package com.mpnet.controllers;

public enum SystemRequest {
	  Handshake(Short.valueOf((short)0)), 
	  Login(Short.valueOf((short)1)), 
	  Logout(Short.valueOf((short)2)), 
	  GetRoomList(Short.valueOf((short)3)),
	  CallExtension(Short.valueOf((short)13)), 

	  KickUser(Short.valueOf((short)24)), 
	  ManualDisconnection(Short.valueOf((short)26)), 
	  PingPong(Short.valueOf((short)29)), 

	
	  OnUserLost(Short.valueOf((short)1002)), 
	  OnClientDisconnection(Short.valueOf((short)1005)), 
	  OnReconnectionFailure(Short.valueOf((short)1006));

	private Object id;

	public static SystemRequest fromId(Object id) {
		SystemRequest req = null;

		for (SystemRequest item : values()) {
			if (!item.getId().equals(id))
				continue;
			req = item;
			break;
		}

		return req;
	}

	private SystemRequest(Object id) {
		this.id = id;
	}

	public Object getId() {
		return this.id;
	}
}