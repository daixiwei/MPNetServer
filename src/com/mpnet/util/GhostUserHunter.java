package com.mpnet.util;

import com.mpnet.MPNetServer;
import com.mpnet.api.IMPApi;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.bitswarm.sessions.ISessionManager;
import com.mpnet.entities.User;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostUserHunter implements IGhostUserHunter {
	private static final String	EOL				= System.getProperty("line.separator");
	private final MPNetServer	mpnet;
	private final IMPApi		api;
	private ISessionManager		sm;
	private final Logger		log;
	private static final int	TOT_CYCLES		= 90;
	private int					cycleCounter	= 0;
	
	public GhostUserHunter() {
		mpnet = MPNetServer.getInstance();
		api = this.mpnet.getAPIManager().getApi();
		log = LoggerFactory.getLogger(getClass());
	}
	
	public void hunt() {
		if (sm == null) {
			sm = this.mpnet.getSessionManager();
		}
		
		if (++cycleCounter < TOT_CYCLES) {
			return;
		}
		cycleCounter = 0;
		
		List<User> ghosts = searchGhosts();
		if (ghosts.size() > 0) {
			log.info(buildReport(ghosts));
		}
		
		for (User ghost : ghosts)
			api.disconnectUser(ghost);
	}
	
	private List<User> searchGhosts() {
		List<User> allUsers = mpnet.getUserManager().getAllUsers();
		List<User> ghosts = new ArrayList<User>();
		
		for (User u : allUsers) {
			ISession sess = u.getSession();
			
			if ((!sm.containsSession(sess)) || (sess.isIdle()) || (sess.isMarkedForEviction())) {
				ghosts.add(u);
			}
		}
		return ghosts;
	}
	
	private String buildReport(List<User> ghosts) {
		StringBuilder sb = new StringBuilder("GHOST REPORT");
		sb.append(EOL).append("Total ghosts: ").append(ghosts.size()).append(EOL);
		
		for (User ghost : ghosts) {
			ISession ss = ghost.getSession();
			
			if (ss == null) {
				sb.append(ghost.getId()).append(", ").append(ghost.getName()).append(" -> Null session").append(", SessionById: ").append(this.sm.getSessionById(ghost.getId()));
			} else {
				sb.append(ghost.getId()).append(", ").append(ghost.getName()).append(", Connected: ").append(ss.isConnected()).append(", Idle: ").append(ss.isIdle()).append(", Marked: ").append(ss.isMarkedForEviction()).append(", Frozen: ").append(ss.isFrozen()).append(", SessionById: ")
						.append(this.sm.getSessionById(ghost.getId()));
			}
		}
		return sb.toString();
	}
}