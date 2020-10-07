package io.openindoormap.domain.extrusionmodel;

import io.openindoormap.domain.common.FileInfo;
import lombok.*;

@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignLayerFileInfo extends FileInfo {

	public static final String ZIP_EXTENSION = "zip";
	public static final String SHAPE_EXTENSION = "shp";

	// 사용자 아이디
	private String userId;
	
	// 고유번호
	private Long designLayerFileInfoId;
	// design layer 고유번호
	private Long designLayerId;
	// design layer name
	private String designLayerName;
	// shape 파일 팀 아이디. .shp 파일의 design_layer_file_info_id 를 team_id로 함
	private Long designLayerFileInfoTeamId;

	// 활성화 유무. Y: 활성화, N: 비활성화
	private String enableYn;

	// shape 파일 인코딩
	private String shapeEncoding;
	// shape file version 정보
	private Integer versionId;
}
