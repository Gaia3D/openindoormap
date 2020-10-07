package io.openindoormap.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.data.DataObjectAttribute;
import io.openindoormap.domain.data.DataObjectAttributeFileInfo;
import io.openindoormap.parser.DataObjectAttributeFileParser;
import io.openindoormap.parser.impl.DataObjectAttributeFileJsonParser;
import io.openindoormap.persistence.DataObjectAttributeMapper;
import io.openindoormap.service.DataObjectAttributeService;
import io.openindoormap.service.DataService;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.utils.FileUtils;

import java.util.Map;

/**
 * 데이터 Object 속성 관리
 * @author jeongdae
 *
 */
@Slf4j
@Service
public class DataObjectAttributeServiceImpl implements DataObjectAttributeService {
	
	@Autowired
	private DataService dataService;
	@Autowired
	private DataObjectAttributeMapper dataObjectAttributeMapper;
	
	/**
	 * 데이터 Object 속성 정보를 취득
	 * @param dataId
	 * @return
	 */
	@Transactional(readOnly=true)
	public DataObjectAttribute getDataObjectAttribute(Long dataId) {
		return dataObjectAttributeMapper.getDataObjectAttribute(dataId);
	}
	
	/**
	 * 데이터 Object 속성 등록
	 * @param dataId
	 * @param dataObjectAttributeFileInfo
	 * @return
	 */
	@Transactional
	public DataObjectAttributeFileInfo insertDataObjectAttribute(Long dataId, DataObjectAttributeFileInfo dataObjectAttributeFileInfo) {
		
		// 파일 이력을 저장
		dataObjectAttributeMapper.insertDataObjectAttributeFileInfo(dataObjectAttributeFileInfo);
		
		DataObjectAttributeFileParser dataObjectAttributeFileParser;
		if(FileUtils.EXTENSION_JSON.equals(dataObjectAttributeFileInfo.getFileExt())) {
			dataObjectAttributeFileParser = new DataObjectAttributeFileJsonParser();
		} else {
			dataObjectAttributeFileParser = new DataObjectAttributeFileJsonParser();
		}
		Map<String, Object> map = dataObjectAttributeFileParser.parse(dataId, dataObjectAttributeFileInfo);
		
		String attribute = (String) map.get("attribute");
		int insertSuccessCount = 0;
		int updateSuccessCount = 0;
		int insertErrorCount = 0;
		try {
			DataObjectAttribute dataObjectAttribute = dataObjectAttributeMapper.getDataObjectAttribute(dataId);
			if(dataObjectAttribute == null) {
				dataObjectAttribute = new DataObjectAttribute();
				dataObjectAttribute.setDataId(dataId);
				dataObjectAttribute.setAttributes(attribute);
				dataObjectAttributeMapper.insertDataObjectAttribute(dataObjectAttribute);
				insertSuccessCount++;
			} else {
				dataObjectAttribute.setAttributes(attribute);
				dataObjectAttributeMapper.updateDataObjectAttribute(dataObjectAttribute);
				updateSuccessCount++;
			}
		} catch(DataAccessException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ dataAccess exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			insertErrorCount++;
		} catch(RuntimeException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ runtime exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			insertErrorCount++;
		} catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			insertErrorCount++;
		}
		
		dataObjectAttributeFileInfo.setTotalCount((Integer) map.get("totalCount"));
		dataObjectAttributeFileInfo.setParseSuccessCount((Integer) map.get("parseSuccessCount"));
		dataObjectAttributeFileInfo.setParseErrorCount((Integer) map.get("parseErrorCount"));
		dataObjectAttributeFileInfo.setInsertSuccessCount(insertSuccessCount);
		dataObjectAttributeFileInfo.setUpdateSuccessCount(updateSuccessCount);
		dataObjectAttributeFileInfo.setInsertErrorCount(insertErrorCount);
		
		dataObjectAttributeMapper.updateDataObjectAttributeFileInfo(dataObjectAttributeFileInfo);
		
		// 데이터 속성 필드 수정
		DataInfo dataInfo = new DataInfo();
		dataInfo.setDataId(dataObjectAttributeFileInfo.getDataId());
		dataInfo.setObjectAttributeExist(true);
		dataService.updateData(dataInfo);
		
		return dataObjectAttributeFileInfo;
	}
}
