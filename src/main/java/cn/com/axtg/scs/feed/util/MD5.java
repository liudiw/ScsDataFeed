/**
 * 
 */
package cn.com.axtg.scs.feed.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Project: ScsDataFeed
 *
 * Filename: MD5.java
 *
 * Creation Date: Sep 14, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class MD5 {

	/**
	 * @param input - String to be encrypted
	 * @return String - encrypted input
	 */
	public static String encrypt(String input) {

		String dstr = null;
		byte[] digest;
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			digest = md.digest();
			dstr = new BigInteger(1, digest).toString(16);

			if (dstr.length() % 2 > 0)
				dstr = "0" + dstr;		
		}
		catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			dstr = null;
		}

		return dstr;
	}
}
