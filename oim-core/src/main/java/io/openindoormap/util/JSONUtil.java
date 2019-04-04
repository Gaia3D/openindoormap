package io.openindoormap.util;

/**
 * @author Cheon JeongDae
 *
 */
public class JSONUtil {

	public static String getResultTreeString(String result, String treeData) {
		return "{\"result\": \"" + result + "\",\"groupTree\":" + treeData + "}";
	}
}
