package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignLayerGroup implements Serializable {

	private static final long serialVersionUID = -1480761819761262002L;

	/****** 화면 표시용 *******/
	// up : 위로, down : 아래로
	private String updateType;

	// 그룹Key 중복 확인 hidden 값
	private String duplicationValue;

	// design layer 그룹 고유번호
	private Integer designLayerGroupId;
	// design layer 그룹 그룹명
	private String designLayerGroupName;
	// 사용자 아이디
	private String userId;
	// 조상
	private Integer ancestor;

	// 부모
	private Integer parent;
	private String parentName;
	// 깊이
	private Integer depth;
	// 나열 순서
	private Integer viewOrder;
	// 자식 존재 유무
	private Integer children;
	// 사용 유무
	private Boolean available;
	// 설명
	private String description;
	
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;

	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
	/**
	 * 자식 design 레이어 목록
	 */
	private List<DesignLayer> designLayerList;
}
