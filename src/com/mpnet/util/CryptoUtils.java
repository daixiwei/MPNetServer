package com.mpnet.util;

import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.util.MD5;
import java.util.Random;

/**
 * 
 * @ClassName: CryptoUtils
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:58:57
 *
 */
public class CryptoUtils {
	private static final String	DELIMITER	= "__";
	
	public static String getClientPassword(ISession session, String clearPass) {
		return MD5.getInstance().getHash(session.getHashId() + clearPass);
	}
	
	public static String getMD5Hash(String str) {
		return MD5.getInstance().getHash(str);
	}
	
	public static String getUniqueSessionToken(ISession session) {
		Random rnd = new Random();
		String key = session.getFullIpAddress() + DELIMITER + String.valueOf(rnd.nextInt());
		
		return MD5.getInstance().getHash(key);
	}
	
	public static String getHexFileName(String name) {
		StringBuilder sb = new StringBuilder();
		
		char[] c = name.toCharArray();
		
		for (int i = 0; i < c.length; i++) {
			sb.append(Integer.toHexString(c[i]));
		}
		
		return sb.toString();
	}
}