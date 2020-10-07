package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "designLayerLands")
public class DesignLayerLandDto {

    // design layer lnad 고유 번호
    private Long designLayerLandId;
    // design layer 고유번호
    private Long designLayerId;
    //  design layer 그룹 고유 번호
    private Integer designLayerGroupId;
    // shape 파일 고유 번호
    private Long identificationCode;
    // 사업유형
    private String projectType;
    // 사업지구
    private String projectTitle;
    // 가구번호
    private String blockCode;
    // 획지번호
    private String lotCode;
    // 획지면적
    private String lotArea;
    // 용도지역
    private String landuseZoning;
    // 토지이용
    private String landusePlan;
    // 대지분할합필
    private String lotDivideMarge;
    // 용도
    private String buildingUse;
    // 용도-지정
    private String buildingUseDefined;
    // 용도-권장
    private String buildingUseRecommended;
    // 용도-허용
    private String buildingUseAllowed;
    // 용도-제한
    private String buildingUseConditional;
    // 용도-불허
    private String buildingUseForbidden;
    // 건폐율
    private String buildingCoverageRatio;
    // 건폐율 기준
    private String buildingCoverageRatioStandard;
    // 용적률
    private String floorAreaRatio;
    // 용적률-기준
    private String floorAreaRatioStandard;
    // 용적률-허용
    private String floorAreaRatioAllowed;
    // 용적률-상한
    private String floorAreaRatioMaximum;
    // 최고높이
    private String maximumBuildingHeight;
    // 최고층수
    private String maximumBuildingFloors;
    // 주택유형
    private String housingType;
    // 세대수
    private String numberOfHouseholds;
    // 기준시점
    private String reference;
    // 속성
    private String properties;
    // 수정일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
    // 등록일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
    // wkt
    private String theGeom;
}
