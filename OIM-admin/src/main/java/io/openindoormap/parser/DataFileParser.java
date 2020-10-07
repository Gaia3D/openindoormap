package io.openindoormap.parser;

import java.util.Map;

import io.openindoormap.domain.data.DataFileInfo;

/**
 * @author Cheon JeongDae
 *
 */
public interface DataFileParser {
	
	/**
	 * @param dataGroupId
	 * @param fileInfo
	 * @return
	 */
	Map<String, Object> parse(Integer dataGroupId, DataFileInfo fileInfo);
}
