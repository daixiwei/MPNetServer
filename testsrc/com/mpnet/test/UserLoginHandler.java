package com.mpnet.test;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.db.IRowMapp;
import com.mpnet.db.TableMapp;
import com.mpnet.entities.MPUser;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPErrorCode;
import com.mpnet.exceptions.MPErrorData;
import com.mpnet.exceptions.MPLoginException;
import com.mpnet.extensions.ILoginHandler;

/**
 * 
 * @ClassName: UserLoginHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月6日 下午4:11:41
 *
 */
public class UserLoginHandler implements ILoginHandler {
	
	@Override
	public User validateLogin(ISession session, String userName, String password, IMPObject paramsIn, IMPObject paramsOut, boolean allowGuestUsers) throws MPLoginException {
		final TableMapp userMapp = MPNetServer.getInstance().getDBManager().getDataMappManager().getTableMapp(UserBean.TABLE_NAME);
		final IRowMapp rowMapp = userMapp.getRowMappByPrimarykey(UserBean.KEY_USERNAME, userName);
		UserBean userBean = null;
		if (rowMapp == null) {
			userBean = new UserBean();
			
			userBean.username = userName;
			userBean.password = password;
			userMapp.addRowMapp(userBean);
			try {
				userBean.commit();
			} catch (Exception e) {
				MPErrorData data = new MPErrorData(MPErrorCode.LOGIN_BAD_PASSWORD);
				throw new MPLoginException("Login failed. Password don't match for User: " + userName, data);
			}
		} else {
			userBean = (UserBean) rowMapp;
			String password1 = userBean.password;
			if (!password1.equalsIgnoreCase(password)) {
				MPErrorData data = new MPErrorData(MPErrorCode.LOGIN_BAD_PASSWORD);
				throw new MPLoginException("Login failed. Password don't match for User: " + userName, data);
			}
		}
		
		int accountId = userBean.getId().intValue();
		
		paramsOut.putInt("accountId", accountId);
		paramsOut.putUtfString("key", session.getHashId());
		paramsOut.putInt("lastServerId", userBean.lastServerId);
		return new MPUser(userName, session);
	}
	
}
