package com.mpnet.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: MD5 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月9日 上午10:14:06 
 *
 */
public final class MD5 {
	private static MD5 _instance = new MD5();
	private MessageDigest messageDigest;
	private final Logger log;

	private MD5() {
		this.log = LoggerFactory.getLogger(getClass());
		try {
			this.messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			this.log.error("Could not instantiate the MD5 Message Digest!");
		}
	}

	public static MD5 getInstance() {
		return _instance;
	}

	public synchronized String getHash(String s) {
		byte[] data = s.getBytes();

		this.messageDigest.update(data);

		return toHexString(this.messageDigest.digest());
	}

	private String toHexString(byte[] byteData) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(byteData[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			sb.append(hex);
		}

		return sb.toString();
	}
}