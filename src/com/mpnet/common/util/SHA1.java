package com.mpnet.common.util;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: SHA1 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月9日 上午10:14:10 
 *
 */
public final class SHA1 {
	private static SHA1 _instance = new SHA1();
	private MessageDigest messageDigest;
	private final Logger log;
	private final Base64Encoder b64;

	private SHA1() {
		this.log = LoggerFactory.getLogger(getClass());
		this.b64 = new Base64Encoder();
		try {
			this.messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			this.log.error("Could not instantiate the SHA-1 Message Digest!");
		}
	}

	public static SHA1 getInstance() {
		return _instance;
	}

	public synchronized String getHash(String s) {
		byte[] data = s.getBytes();
		this.messageDigest.update(data);

		return toHexString(this.messageDigest.digest());
	}

	public synchronized String getBase64Hash(String s) {
		byte[] data = s.getBytes();
		this.messageDigest.update(data);

		return this.b64.encode(this.messageDigest.digest());
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