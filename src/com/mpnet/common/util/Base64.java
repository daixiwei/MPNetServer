package com.mpnet.common.util;

import java.util.Arrays;

/**
 * 
 * @ClassName: Base64
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月9日 上午10:11:20
 *
 */
public class Base64 {
	private static final char[]	CA	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private static final int[]	IA	= new int[256];
	
	static {
		Arrays.fill(IA, -1);
		int i = 0;
		for (int iS = CA.length; i < iS; i++) {
			IA[CA[i]] = i;
		}
		IA[61] = 0;
	}
	
	/**
	 * 
	 * @param sArr
	 * @param lineSep
	 * @return
	 */
	private static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
		int sLen = sArr != null ? sArr.length : 0;
		if (sLen == 0) {
			return new char[0];
		}
		int eLen = sLen / 3 * 3;
		int cCnt = (sLen - 1) / 3 + 1 << 2;
		int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0);
		char[] dArr = new char[dLen];
		
		int s = 0;
		int d = 0;
		for (int cc = 0; s < eLen;) {
			int i = (sArr[(s++)] & 0xFF) << 16 | (sArr[(s++)] & 0xFF) << 8 | sArr[(s++)] & 0xFF;
			
			dArr[(d++)] = CA[(i >>> 18 & 0x3F)];
			dArr[(d++)] = CA[(i >>> 12 & 0x3F)];
			dArr[(d++)] = CA[(i >>> 6 & 0x3F)];
			dArr[(d++)] = CA[(i & 0x3F)];
			if (lineSep) {
				cc++;
				if ((cc == 19) && (d < dLen - 2)) {
					dArr[(d++)] = '\r';
					dArr[(d++)] = '\n';
					cc = 0;
				}
			}
		}
		int left = sLen - eLen;
		if (left > 0) {
			int i = (sArr[eLen] & 0xFF) << 10 | (left == 2 ? (sArr[(sLen - 1)] & 0xFF) << 2 : 0);
			
			dArr[(dLen - 4)] = CA[(i >> 12)];
			dArr[(dLen - 3)] = CA[(i >>> 6 & 0x3F)];
			dArr[(dLen - 2)] = (left == 2 ? CA[(i & 0x3F)] : '=');
			dArr[(dLen - 1)] = '=';
		}
		return dArr;
	}
	
	/**
	 * 
	 * @param sArr
	 * @param lineSep
	 * @return
	 */
	public static final String encodeToString(byte[] sArr, boolean lineSep) {
		return new String(encodeToChar(sArr, lineSep));
	}
	
	/**
	 * 
	 * @param sArr
	 * @return
	 */
	public static final String encodeToString(byte[] sArr) {
		return encodeToString(sArr, false);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static final byte[] decode(String str) {
		int sLen = str != null ? str.length() : 0;
		if (sLen == 0) {
			return new byte[0];
		}
		int sepCnt = 0;
		for (int i = 0; i < sLen; i++) {
			if (IA[str.charAt(i)] < 0) {
				sepCnt++;
			}
		}
		if ((sLen - sepCnt) % 4 != 0) {
			return null;
		}
		int pad = 0;
		for (int i = sLen; (i > 1) && (IA[str.charAt(--i)] <= 0);) {
			if (str.charAt(i) == '=') {
				pad++;
			}
		}
		int len = ((sLen - sepCnt) * 6 >> 3) - pad;
		
		byte[] dArr = new byte[len];
		
		int s = 0;
		for (int d = 0; d < len;) {
			int i = 0;
			for (int j = 0; j < 4; j++) {
				int c = IA[str.charAt(s++)];
				if (c >= 0) {
					i |= c << 18 - j * 6;
				} else {
					j--;
				}
			}
			dArr[(d++)] = ((byte) (i >> 16));
			if (d < len) {
				dArr[(d++)] = ((byte) (i >> 8));
				if (d < len) {
					dArr[(d++)] = ((byte) i);
				}
			}
		}
		return dArr;
	}
}
