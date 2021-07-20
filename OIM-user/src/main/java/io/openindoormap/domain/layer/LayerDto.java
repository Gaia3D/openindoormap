package io.openindoormap.domain.layer;

import lombok.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * design layer
 *
 * @author Cheon JeongDae
 */
@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "layers")
public class LayerDto implements Serializable {

    private static final long serialVersionUID = -5868177119090810270L;

    // 고유키
    @ApiModelProperty(value = "고유키")
    private Integer layerId;
    // 레이어 그룹 아이디
    @ApiModelProperty(value = "레이어 그룹 아이디")
    private Integer layerGroupId;
    // 레이어 키
    @ApiModelProperty(value = "레이어 키")
    private String layerKey;
    // 레이어 명
    @ApiModelProperty(value = "레이어 명")
    private String layerName;
    // 레이어 그룹명
    @ApiModelProperty(value = "레이어 그룹명")
    private String layerGroupName;
    // 업로딩 아이디
    @ApiModelProperty(value = "업로딩 아이디")
    private String userId;
    // 공유 타입. 0 : 공개, 1 : 개인, 2 : 그룹
    @ApiModelProperty(value = "공유 타입")
    private String sharing;
    // OGC Web Services (wms, wfs, wcs, wps)
    @ApiModelProperty(value = "OGC Web Services")
    private String ogcWebServices;
    // Raster, Vector
    @ApiModelProperty(value = "레이어 타입")
    private String layerType;
    // 레이어 등록 타입(파일, geoserver)
    @ApiModelProperty(value = "레이어 등록 타입")
    private String layerInsertType;
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
    @Range(min = 1, max = 5)
    private Float layerLineStyle;
    // 레이어 투명도
    @ApiModelProperty(value = "레이어 투명도")
    @Range(min = 1, max = 100)
    private Float layerAlphaStyle;
    // 나열 순서
    @ApiModelProperty(value = "나열 순서")
    private Integer viewOrder;
    // 지도 레이어 표시 우선 순위
    @ApiModelProperty(value = "지도 레이어 표시 우선 순위")
    private Integer zIndex;

    public Integer getViewZIndex() {
        return this.zIndex;
    }
    
    // 기본표시
    @ApiModelProperty(value = "기본표시")
    private Boolean defaultDisplay;
    // 사용 유무
    @ApiModelProperty(value = "사용 유무")
    private Boolean available;
    // 레이블 표시 유무. Y : 표시, N : 비표시(기본값)
    @ApiModelProperty(value = "레이블 표시 유무")
    private Boolean labelDisplay;
    // 캐시 사용 유무
    @ApiModelProperty(value = "캐시 사용 유무")
    private Boolean cacheAvailable;
    
    // 좌표계
    @ApiModelProperty(value = "좌표계")
    private String coordinate;
    // 설명
    @ApiModelProperty(value = "설명")
    private String description;

    @ApiModelProperty(value = "수정일")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "입력일")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
    
}
