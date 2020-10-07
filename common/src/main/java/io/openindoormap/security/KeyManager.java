package io.openindoormap.security;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@Slf4j
public class KeyManager {

	private static final String randomKeyword = "dGRobCNAISBzaSBlbWFuIHltIC50ZGhsQCEgcm9mIGFlZGkgZGFiIGEgZWthbSB0b24gb2QgZXNhZWxwICx5ZWsgdGVyY3MjIGRudW9mIGV2YWggdW95IGZJ";

	public static String getInitKey() {
		String result = null;
		try {
			byte[] base64decodedBytes = Base64.getDecoder().decode(randomKeyword.getBytes("UTF-8"));
			result = new String(base64decodedBytes, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			log.info("UnsupportedEncodingException ===== {} ", e.getMessage());
		}
		result = reverseString(result);
		
		return parse(result);
	}
	
	private static String reverseString(String value) {
		if(value == null) return "";
		return (new StringBuffer(value)).reverse().toString();
	}
	
	private static String parse(String value) {
		return value.substring(83, 90) + value.substring(18, 24) + value.substring(25, 28);
	}
}
