package io.openindoormap.parser;

import java.util.Map;

import io.openindoormap.domain.data.DataAttributeFileInfo;

/**
 * @author Cheon JeongDae
 *
 */
public interface DataAttributeFileParser {

	/**
	 *
	 * @param dataId
	 * @param dataAttributeFileInfo
	 * @return
	 */
	Map<String, Object> parse(Long dataId, DataAttributeFileInfo dataAttributeFileInfo);
}
