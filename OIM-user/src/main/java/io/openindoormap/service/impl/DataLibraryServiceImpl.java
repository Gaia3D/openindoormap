package io.openindoormap.service.impl;

import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.persistence.DataLibraryMapper;
import io.openindoormap.service.DataLibraryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 데이터 라이브러리
 * @author jeongdae
 *
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataLibraryServiceImpl implements DataLibraryService {

	private final DataLibraryMapper dataLibraryMapper;
	
	/**
	 * 데이터 라이브러리 목록
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<DataLibrary> getListDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.getListDataLibrary(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public DataLibrary getDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.getDataLibrary(dataLibrary);
	}
}
