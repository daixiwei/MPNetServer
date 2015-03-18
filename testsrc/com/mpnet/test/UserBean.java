package com.mpnet.test;

import java.io.Serializable;
import com.mpnet.db.BaseRowMapp;

/**
 * @ClassName: UserBean
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月7日 下午1:18:28
 * 
 */
public class UserBean extends BaseRowMapp implements Serializable {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3716955303831317297L;
	public static final String	TABLE_NAME			= "users";
	public static final String	KEY_USERNAME		= "username";
	
	public int					id;
	public String				username;
	public String				password;
	public int					lastServerId;
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer value) {
		this.id = value;
	}
	
}
