package io.openindoormap.domain.data;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "dataGroups")
public class DataGroupDto implements Serializable {
	
	private static final long serialVersionUID = 4874076788913826887L;

	// 고유번호
	@ApiModelProperty(value = "고유번호")
	private Integer dataGroupId;
	
	// 링크 활용 등을 위한 확장 컬럼
	@ApiModelProperty(value = "링크 활용 등을 위한 확장 컬럼")
	@Size(max = 60)
	private String dataGroupKey;
	
	// 그룹명
	@ApiModelProperty(value = "그룹명")
	@Size(max = 100)
	private String dataGroupName;
	
	// 서비스 경로
	@ApiModelProperty(value = "서비스 경로")
	private String dataGroupPath;
	
	// admin : 관리자용 데이터 그룹, user : 일반 사용자용 데이터 그룹
	@ApiModelProperty(value = "admin : 관리자용 데이터 그룹, user : 일반 사용자용 데이터 그룹")
	private String dataGroupTarget;
	// 공유 타입. common : 공통, public : 공개, private : 개인, group : 그룹
	@ApiModelProperty(value = "공유 타입")
	private String sharing;
	// 사용자명
	@ApiModelProperty(value = "사용자명")
	private String userId;
	
	// 조상
	@ApiModelProperty(value = "조상")
	private Integer ancestor;
	// 부모
	@ApiModelProperty(value = "부모")
	private Integer parent;
	// 깊이
	@ApiModelProperty(value = "깊이")
	private Integer depth;
	
	// 순서
	@ApiModelProperty(value = "순서")
	private Integer viewOrder;
	// 자식 존재 유무
	@ApiModelProperty(value = "자식 존재 유무")
	private Integer children;
		
	// true : 기본, false : 선택
	@ApiModelProperty(value = "true : 기본, false : 선택")
	private Boolean basic;
	// true : 사용, false : 사용안함
	@ApiModelProperty(value = "true : 사용, false : 사용안함")
	private Boolean available;
	// 스마트 타일링 사용유무. true : 사용, false : 사용안함(기본)
	@ApiModelProperty(value = "스마트 타일링 사용유무")
	private Boolean tiling;
		
	// 데이터 총 건수
	@ApiModelProperty(value = "데이터 총 건수")
	private Integer dataCount;
		
	// POINT(위도, 경도). 공간 검색 속도 때문에 altitude는 분리
	@ApiModelProperty(value = "POINT(위도, 경도)")
	private String location;
	// 높이
	@ApiModelProperty(value = "높이")
	private BigDecimal altitude;
	// Map 이동시간
	@ApiModelProperty(value = "Map 이동시간")
	private Integer duration;
	
	// location 업데이트 방법. auto : data 입력시 자동, user : 사용자가 직접 입력
	@ApiModelProperty(value = "location 업데이트 방법")
	private String locationUpdateType;
	// 데이터 그룹 메타 정보. 그룹 control을 위해 인위적으로 만든 속성
	@ApiModelProperty(value = "데이터 그룹 메타 정보")
	private String metainfo;
	// 설명
	@ApiModelProperty(value = "설명")
	private String description;
	
	// 수정일
	@ApiModelProperty(value = "수정일")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;
	// 등록일
	@ApiModelProperty(value = "등록일")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;

	
}
