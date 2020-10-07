package io.openindoormap.service.impl;
//package lhdt.service.impl;
//
//import lhdt.domain.urban.Urban;
//import lhdt.persistence.NewTownMapper;
//import lhdt.service.UrbanService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * 뉴타운
// * @author jeongdae
// *
// */
//@Service
//public class UrbanServiceImpl implements UrbanService {
//
//	@Autowired
//	private NewTownMapper newTownMapper;
//
//	/**
//	 * 뉴타운 수
//	 * @param newTown
//	 * @return
//	 */
//	@Transactional(readOnly=true)
//	public Long getNewTownTotalCount(Urban newTown) {
//		return newTownMapper.getNewTownTotalCount(newTown);
//	}
//
//	/**
//	 * 뉴타운 목록
//	 * @param newTown
//	 * @return
//	 */
//	@Transactional(readOnly=true)
//	public List<Urban> getListNewTown(Urban newTown) {
//		return newTownMapper.getListNewTown(newTown);
//	}
//
//	/**
//	 * 뉴타운 정보
//	 * @param newTownId
//	 * @return
//	 */
//	@Transactional(readOnly=true)
//	public Urban getNewTown(Integer newTownId) {
//		return newTownMapper.getNewTown(newTownId);
//	}
//
//	/**
//	 * 뉴타운 등록
//	 * @param newTown
//	 * @return
//	 */
//	@Transactional
//	public int insertNewTown(Urban newTown) {
//		return newTownMapper.insertNewTown(newTown);
//	}
//
//	/**
//	 * 뉴타운 수정
//	 * @param newTown
//	 * @return
//	 */
//	@Transactional
//	public int updateNewTown(Urban newTown) {
//		return newTownMapper.updateNewTown(newTown);
//	}
//
//	/**
//	 * 뉴타운 삭제
//	 * @param  newTownId
//	 * @return
//	 */
//	@Transactional
//	public int deleteNewTown(Integer newTownId) {
//		return newTownMapper.deleteNewTown( newTownId);
//	}
//}
