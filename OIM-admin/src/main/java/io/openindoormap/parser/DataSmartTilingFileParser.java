package io.openindoormap.parser;

import java.util.Map;

import io.openindoormap.domain.data.DataSmartTilingFileInfo;

/**
 * @author Cheon JeongDae
 *
 */
public interface DataSmartTilingFileParser {

	/**
	 *
	 * @param dataGroupId
	 * @param fileInfo
	 * @return
	 */
	Map<String, Object> parse(Integer dataGroupId, DataSmartTilingFileInfo fileInfo);
}
