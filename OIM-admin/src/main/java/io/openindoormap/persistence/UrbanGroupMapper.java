package io.openindoormap.persistence;

import io.openindoormap.domain.urban.UrbanGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrbanGroupMapper {

	/**
     * 도시 그룹 목록
     * @return
     */
    List<UrbanGroup> getListUrbanGroup();

    /**
     * 도시 정보 조회
     * @param urbanGroup
     * @return
     */
    UrbanGroup getUrbanGroup(UrbanGroup urbanGroup);

    /**
     * 부모와 표시 순서로 메뉴 조회
     * @param urbanGroup
     * @return
     */
    UrbanGroup getUrbanGroupByParentAndViewOrder(UrbanGroup urbanGroup);

	/**
	 * 도시 그룹 Key 중복 확인
	 * @param urbanGroup
	 * @return
	 */
	Boolean isUrbanGroupKeyDuplication(UrbanGroup urbanGroup);

    /**
     * 도시 그룹 등록
     * @param urbanGroup
     * @return
     */
    int insertUrbanGroup(UrbanGroup urbanGroup);

	/**
	 * 도시 그룹 수정
	 * @param urbanGroup
	 * @return
	 */
	int updateUrbanGroup(UrbanGroup urbanGroup);

    /**
	 * 도시 그룹 표시 순서 수정 (up/down)
	 * @param urbanGroup
	 * @return
	 */
	int updateUrbanGroupViewOrder(UrbanGroup urbanGroup);

	/**
	 * 도시 그룹 삭제
	 * @param urbanGroup
	 * @return
	 */
	int deleteUrbanGroup(UrbanGroup urbanGroup);

	/**
	 * ancestor를 이용하여 도시 그룹 삭제
	 * @param urbanGroup
	 * @return
	 */
	int deleteUrbanGroupByAncestor(UrbanGroup urbanGroup);

	/**
	 * parent를 이용하여 도시 그룹 삭제
	 * @param urbanGroup
	 * @return
	 */
	int deleteUrbanGroupByParent(UrbanGroup urbanGroup);
}
