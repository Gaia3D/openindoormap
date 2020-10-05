package io.openindoormap.util;

/**
 *
 */
public class JSONUtil {

	public static String getResultTreeString(String result, String treeData) {
		return "{\"result\": \"" + result + "\",\"groupTree\":" + treeData + "}";
	}
}
