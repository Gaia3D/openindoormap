package io.openindoormap.domain.layer;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import io.openindoormap.domain.common.Search;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 레이어
 * @author Cheon JeongDae
 *
 */
@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Layer extends Search implements Serializable {

    /**
    * 레이어 목록 표시
    */
    private static final long serialVersionUID = -4668268071028609827L;

    // 경도
    @ApiModelProperty(value = "경도")
    private String longitude;
    // 위도
    @ApiModelProperty(value = "위도")
    private String latitude;
    // POINT
    @ApiModelProperty(value = "POINT")
    private String point;
    
    // 리스트 펼치기
    @ApiModelProperty(value = "리스트 펼치기")
    private String open;
    // 계층 타입
    @ApiModelProperty(value = "계층 타입")
    private String nodeType;
    
    // 수정 유형
    @ApiModelProperty(value = "수정 유형")
    private String updateType;
    // 쓰기 모드
    @ApiModelProperty(value = "쓰기 모드")
    private String writeMode;
    
    // 화면 ui
    @ApiModelProperty(value = "부모")
    private Integer parent;
    @ApiModelProperty(value = "부모 이름")
    private String parentName;
    @ApiModelProperty(value = "깊이")
    private Integer depth;
    // 순서
    @ApiModelProperty(value = "순서")
    private Integer viewOrder;
    
    // shape file 인코딩
    @ApiModelProperty(value = "shape file 인코딩")
    private String shapeEncoding;
    
	// style file 내용
    @ApiModelProperty(value = "style file 내용")
	private String styleFileContent;
    
    // DB
    // layer 아이디
    @ApiModelProperty(value = "layer 아이디")
    private Integer layerId;
    // layer 그룹 아이디
    @ApiModelProperty(value = "layer 그룹 아이디")
    private Integer layerGroupId;
    // layer 그룹 명
    @ApiModelProperty(value = "layer 그룹 명")
    private String layerGroupName;
    // layer 키
    @ApiModelProperty(value = "layer 키")
    private String layerKey;
    // layer 명
    @ApiModelProperty(value = "layer 명")
    private String layerName;
    
    // 업로딩 아이디
    @ApiModelProperty(value = "업로딩 아이디")
    private String userId;
    
    // 공유 타입. 0 : 공개, 1 : 개인, 2 : 그룹
    @ApiModelProperty(value = "공유 타입")
    private String sharing;
    // OGC Web Services (wms, wfs, wcs, wps)
    @ApiModelProperty(value = "OGC Web Services")
    private String ogcWebServices;
    // 레이어 타입 (Raster, Vector)	
    @ApiModelProperty(value = "레이어 타입")
    private String layerType;
    // 도형 타입 (point, line, polygon)
    @ApiModelProperty(value = "도형 타입")
    private String geometryType;
    
    // 레이어 색상
    @ApiModelProperty(value = "레이어 색상")
    private String layerFillColor;
    // 레이어 선 색상
    @ApiModelProperty(value = "레이어 선 색상")
    private String layerLineColor;
    // 레이어 선 스타일(두께)
    @ApiModelProperty(value = "레이어 선 스타일(두께)")
    @Range(min=1, max=5)	
    private Float layerLineStyle;
    // 레이어 투명도
    @ApiModelProperty(value = "레이어 투명도")
    @Range(min=1, max=100)	
    private Float layerAlphaStyle;
    
    // 지도 레이어 표시 우선 순위
    @ApiModelProperty(value = "지도 레이어 표시 우선 순위")
    private Integer zIndex;
    public Integer getViewZIndex() {
        return this.zIndex;
    }

    // 기본 표시 여부
    @ApiModelProperty(value = "기본 표시 여부")
    private Boolean defaultDisplay;
    // 사용 유무
    @ApiModelProperty(value = "사용 유무")
    private Boolean available;
    // 레이블 표시 유무. Y : 표시, N : 비표시(기본값)
    @ApiModelProperty(value = "레이블 표시 유무")
    private Boolean labelDisplay;
    @ApiModelProperty(value = "캐시 사용 유무")
    private Boolean cacheAvailable;
    // 좌표계
    @ApiModelProperty(value = "좌표계")
    private String coordinate;
    // 설명
    @ApiModelProperty(value = "설명")
    private String description;
    
    @ApiModelProperty(value = "수정일")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;

    @ApiModelProperty(value = "입력일")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
}
