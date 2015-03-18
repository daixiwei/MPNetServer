package com.mpnet.entities.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.config.ServerSettings;
import com.mpnet.config.ServerSettings.UserSettings;
import com.mpnet.core.BaseCoreService;
import com.mpnet.entities.MPUser;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPErrorCode;
import com.mpnet.exceptions.MPErrorData;
import com.mpnet.exceptions.MPLoginException;
import com.mpnet.exceptions.MPLoginInterruptedException;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.extensions.ILoginHandler;
import com.mpnet.util.IWordFilter;
import com.mpnet.util.MPWordFilter;

/**
 * 
 * @ClassName: MPUserManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:23:57 
 *
 */
public final class MPUserManager extends BaseCoreService implements IUserManager {
	private final ConcurrentMap<String, User> usersByName;
	private final ConcurrentMap<ISession, User> usersBySession;
	private final ConcurrentMap<Integer, User> usersById;
	private IWordFilter wordFilter;
	private Logger logger;
	private int highestCCU = 0;
	private volatile int userReconnectionSeconds = 0;
	private boolean customLogin = false;
	private boolean forceLogout = false;
	private MPNetServer mpnet;
	private UserSettings userSettings;
	
	public MPUserManager() {
		logger = LoggerFactory.getLogger(getClass());
		usersBySession = new ConcurrentHashMap<ISession, User>();
		usersByName = new ConcurrentHashMap<String, User>();
		usersById = new ConcurrentHashMap<Integer, User>();
	}
	
	public void init(Object o){
		mpnet = MPNetServer.getInstance();
		userSettings = mpnet.getConfigurator().getServerSettings().userSettings;
		userReconnectionSeconds = userSettings.userReconnectionSeconds;
		customLogin = userSettings.isCustomLogin;
		forceLogout = userSettings.isForceLogout;
		
		logger.info("User service start!");
		
		name = "UserManagerService";
		active = true;
		
		wordFilter = new MPWordFilter();
		configureWordsFilter(mpnet.getConfigurator().getServerSettings().wordsFilter);
	}

	public void addUser(User user) {
		if (containsId(user.getId())) {
			throw new MPRuntimeException("Can't add User: " + user.getName());
		}
		this.usersById.put(Integer.valueOf(user.getId()), user);
		this.usersByName.put(user.getName(), user);
		this.usersBySession.put(user.getSession(), user);

		if (this.usersById.size() > this.highestCCU)
			this.highestCCU = this.usersById.size();
	}

	public User getUserById(int id) {
		return (User) this.usersById.get(Integer.valueOf(id));
	}

	public User getUserByName(String name) {
		return (User) usersByName.get(name);
	}

	public User getUserBySession(ISession session) {
		return (User) usersBySession.get(session);
	}

	public void removeUser(int userId) {
		User user = (User) usersById.get(Integer.valueOf(userId));

		if (user == null)
			this.logger.warn("Can't remove user with ID: " + userId + ". User was not found.");
		else
			removeUser(user);
	}

	public void removeUser(ISession session) {
		User user = usersBySession.get(session);

		if (user == null) {
			throw new MPRuntimeException("Can't remove user with session: " + session + ". User was not found.");
		}
		removeUser(user);
	}

	public void removeUser(User user) {
		usersById.remove(Integer.valueOf(user.getId()));
		usersByName.remove(user.getName());
		usersBySession.remove(user.getSession());
	}

	public boolean containsId(int userId) {
		return usersById.containsKey(Integer.valueOf(userId));
	}

	public boolean containsSessions(ISession session) {
		return this.usersBySession.containsKey(session);
	}

	public boolean containsUser(User user) {
		return this.usersById.containsValue(user);
	}

	public List<User> getAllUsers() {
		return new ArrayList<User>(this.usersById.values());
	}

	public List<ISession> getAllSessions() {
		return new ArrayList<ISession>(this.usersBySession.keySet());
	}

	public int getUserCount() {
		return usersById.values().size();
	}

	public void disconnectUser(int userId) {
		User user = (User) this.usersById.get(Integer.valueOf(userId));

		if (user == null)
			this.logger.warn("Can't disconnect user with id: " + userId + ". User was not found.");
		else
			disconnectUser(user);
	}

	public void disconnectUser(ISession session) {
		User user = (User) this.usersBySession.get(session);

		if (user == null)
			this.logger.warn("Can't disconnect user with session: " + session + ". User was not found.");
		else
			disconnectUser(user);
	}

	public void disconnectUser(User user) {
		removeUser(user);
	}

	public int getHighestCCU() {
		return this.highestCCU;
	}
	
	public IWordFilter getWordFilter(){
		return this.wordFilter;
	}
	
	
	@Override
	public int getUserReconnectionSeconds() {
		return this.userReconnectionSeconds;
	}

	@Override
	public void setUserReconnectionSeconds(int seconds) {
		this.userReconnectionSeconds = seconds;
	}
	
	@Override
	public boolean isCustomLogin() {
		return customLogin;
	}

	@Override
	public void setCustomLogin(boolean customLogin) {
		this.customLogin =customLogin;
	}
	
	
	@Override
	public User login(ISession session, String userName, String password, IMPObject paramsIn,IMPObject paramsOut, boolean forceLogout) throws MPLoginException {
		boolean isEmptyName = userName.length() == 0;

		if (!isActive()) {
			MPErrorData errorData = new MPErrorData(MPErrorCode.LOGIN_INACTIVE_ZONE);
			throw new MPLoginException("UserManager: " + getName() + " is not active!", errorData);
		}

		if (forceLogout && this.forceLogout) {
			applyForceLogin(userName);
		}
		if (getUserCount() >= userSettings.maxUsers) {
			MPErrorData errorData = new MPErrorData(MPErrorCode.LOGIN_SERVER_FULL);
			throw new MPLoginException("The is full, can't login user: " + userName, errorData);
		}
		boolean allowGuestUsers = userSettings.allowGuestUsers && isEmptyName;
		if (!userSettings.allowGuestUsers && isEmptyName) {
			MPErrorData errorData = new MPErrorData(MPErrorCode.LOGIN_GUEST_NOT_ALLOWED);
			throw new MPLoginException("Guest users are not allowed : " , errorData);
		}

		if ((!isEmptyName) && (getUserByName(userName) != null)) {
			MPErrorData errorData = new MPErrorData(MPErrorCode.LOGIN_ALREADY_LOGGED);
			throw new MPLoginException("Another user is already logged with the same name: " + userName, errorData);
		}
		User user  = null;
		ILoginHandler loginHandler = mpnet.getExtensionManager().getExtension().getLoginHandler();
		if(isCustomLogin()&&loginHandler!=null){
			user = loginHandler.validateLogin(session, userName, password, paramsIn,paramsOut,allowGuestUsers);
		}else{
			user = new MPUser(session);
		}
		if (allowGuestUsers) {
			userName = userSettings.guestUserNamePrefix + user.getId();
			user.setPrivilegeId((short)100);
		}
		user.setLastLoginTime(System.currentTimeMillis());
		user.setReconnectionSeconds(userSettings.userReconnectionSeconds);
		user.setName(userName);
		if (userSettings.overrideMaxUserIdleTime > 0) {
			user.getSession().setMaxLoggedInIdleTime(userSettings.overrideMaxUserIdleTime);
		}

		manageNewUser(user);


		return user;
	}
	
	private void applyForceLogin(String userName) {
		User oldUser = getUserByName(userName);
		if (oldUser == null) {
			return;
		}
		oldUser.getSession().setReconnectionSeconds(0);
		logger.info("User already logged in. Disconnecting previous instance : " + oldUser);
		mpnet.getAPIManager().getApi().disconnectUser(oldUser);

		throw new MPLoginInterruptedException();
	}
	
	private synchronized void manageNewUser(User user) throws MPLoginException {
		boolean duplicateCheck = mpnet.getUserManager().getUserBySession(user.getSession()) != null;

		if (duplicateCheck) {
			MPErrorData errorData = new MPErrorData(MPErrorCode.LOGIN_ALREADY_LOGGED);
			throw new MPLoginException("Duplicate login: " + user, errorData);
		}

		addUser(user);
	}
	
	private void configureWordsFilter(ServerSettings.WordFilterSettings settings) {
		wordFilter.setMaskCharacter(settings.hideBadWordWithCharacter);
		wordFilter.setName("WordFilter");
		wordFilter.setWordsFile(settings.wordsFile);
		wordFilter.setActive(settings.isActive);
		wordFilter.init(null);
	}

	
}