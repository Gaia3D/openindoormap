package io.openindoormap.domain.layer;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "layerGroups")
public class LayerGroupDto implements Serializable {

	private static final long serialVersionUID = 4874076788913826887L;

	// layer 그룹 고유번호
	@ApiModelProperty(value = "layer 그룹 고유번호")
	private Integer layerGroupId;
	// layer 그룹 그룹명
	@ApiModelProperty(value = "layer 그룹 그룹명")
	private String layerGroupName;
	// 사용자 아이디
	@ApiModelProperty(value = "사용자 아이디")
	private String userId;
	// 조상
	@ApiModelProperty(value = "조상")
	private Integer ancestor;

	// 부모
	@ApiModelProperty(value = "부모")
	private Integer parent;
	@ApiModelProperty(value = "부모 명")
	private String parentName;
	// 깊이
	@ApiModelProperty(value = "깊이")
	private Integer depth;
	// 나열 순서
	@ApiModelProperty(value = "나열 순서")
	private Integer viewOrder;
	// 자식 존재 유무
	@ApiModelProperty(value = "자식 존재 유무")
	private Integer children;
	// 사용 유무
	@ApiModelProperty(value = "사용 유무")
	private Boolean available;
	// 설명
	@ApiModelProperty(value = "설명")
	private String description;
	
	@ApiModelProperty(value = "수정일")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;

	@ApiModelProperty(value = "입력일")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
	/**
	 * 자식 design 레이어 목록
	 */
	@ApiModelProperty(value = "자식 design 레이어 목록")
	private List<Layer> layerList;
}
