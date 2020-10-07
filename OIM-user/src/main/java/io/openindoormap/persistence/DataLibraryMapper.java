package io.openindoormap.persistence;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.extrusionmodel.DataLibrary;

import java.util.List;

/**
 * 데이터 라이브러리
 * @author jeongdae
 *
 */
@Repository
public interface DataLibraryMapper {

	/**
	 * 데이터 라이브러리 목록
	 * @param dataLibrary
	 * @return
	 */
	List<DataLibrary> getListDataLibrary(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	DataLibrary getDataLibrary(DataLibrary dataLibrary);
}
