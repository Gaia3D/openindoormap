package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 데이터 Library 그룹
 * @author Cheon JeongDae
 *
 */
@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "dataLibraryGroups")
public class DataLibraryGroupDto {

	// 고유번호
	private Integer dataLibraryGroupId;
	// 링크 활용 등을 위한 확장 컬럼
	private String dataLibraryGroupKey;
	// old 고유 식별번호
	private String oldDataLibraryGroupKey;
	// 그룹명
	@Size(max = 256)
	private String dataLibraryGroupName;
	// 서비스 경로
	private String dataLibraryGroupPath;
	// admin : 관리자용 데이터 library 그룹, user : 일반 사용자용 데이터 library 그룹
	private String dataLibraryGroupTarget;
	// 공유 타입. common : 공통, public : 공개, private : 개인, group : 그룹
	private String sharing;
	// 사용자명
	private String userId;
	private String userName;
	// 조상
	private Integer ancestor;
	// 부모
	private Integer parent;
	// 깊이
	private Integer depth;
	// 순서
	private Integer viewOrder;
	// 자식 존재 유무
	private Integer children;

	// true : 기본, false : 선택
	private Boolean basic;
	// true : 사용, false : 사용안함
	private Boolean available;

	// 데이터 library 총 건수
	private Integer dataLibraryCount;
	// 설명
	private String description;
	
	// 수정일
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;
	// 등록일
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
}
