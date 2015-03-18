package com.mpnet.entities.managers;

import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.core.ICoreService;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPLoginException;
import com.mpnet.util.IWordFilter;
import java.util.List;

/**
 * 
 * @ClassName: IUserManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:22:12 
 *
 */
public interface IUserManager extends ICoreService {
	/**
	 * 
	 * @param name
	 * @return
	 */
	public User getUserByName(String name);

	/**
	 * 
	 * @param paramInt
	 * @return
	 */
	public User getUserById(int paramInt);

	/**
	 * 
	 * @param session
	 * @return
	 */
	public User getUserBySession(ISession session);

	/**
	 * 
	 * @return
	 */
	public List<User> getAllUsers();

	/**
	 * 
	 * @return
	 */
	public List<ISession> getAllSessions();

	/**
	 * 
	 * @param user
	 */
	public void addUser(User user);

	/**
	 * 
	 * @param user
	 */
	public void removeUser(User user);

	/**
	 * 
	 * @param id
	 */
	public void removeUser(int id);
	
	/**
	 * 
	 * @param session
	 */
	public void removeUser(ISession session);
	
	/**
	 * 
	 * @param user
	 */
	public void disconnectUser(User user);

	/**
	 * 
	 * @param id
	 */
	public void disconnectUser(int id);
	
	/**
	 * 
	 * @param session
	 */
	public void disconnectUser(ISession session);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean containsId(int id);

	/**
	 * 
	 * @param session
	 * @return
	 */
	public boolean containsSessions(ISession session);
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public boolean containsUser(User user);

	/**
	 * 
	 * @return
	 */
	public int getUserCount();

	/**
	 * 
	 * @return
	 */
	public int getHighestCCU();
	
	/**
	 * 
	 * @return
	 */
	public IWordFilter getWordFilter();
	

	
	/**
	 * 
	 * @param session
	 * @param userName
	 * @param password
	 * @param paramsIn
	 * @param paramsOut
	 * @param forceLogout
	 * @return
	 * @throws MPLoginException
	 */
	public User login(ISession session, String userName, String password, IMPObject paramsIn, IMPObject paramsOut, boolean forceLogout) throws MPLoginException;
	
	/**
	 * 
	 * @return
	 */
	public int getUserReconnectionSeconds();

	/**
	 * 
	 * @param seconds
	 */
	public void setUserReconnectionSeconds(int seconds) ;
	
	/**
	 * 
	 * @return
	 */
	public boolean isCustomLogin();
	
	/**
	 * 
	 * @param customLogin
	 * @return
	 */
	public void setCustomLogin(boolean customLogin);
}