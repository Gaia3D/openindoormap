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
@Relation(collectionRelation = "designLayerBuildings")
public class DesignLayerBuildingDto {

    // design layer building 고유번호
    private Long designLayerBuildingId;
    // design layer 고유 번호
    private Long designLayerId;
    // design layer 그룹 고유 번호
    private Integer designLayerGroupId;
    // shape 파일 고유 번호
    private Long buildId;
    // 빌딩명
    private String buildName;
    // 빌딩높이
    private String buildHeight;
    // 빌딩층수
    private String buildFloor;
    // 빌딩면적
    private String buildArea;
    // 복합건물 여부
    private String buildComplex;
    // 부모식별키
    private String parentId;
    // 유닛 타입
    private String buildUnitType;
    // 유닛 타입이 사용된 개수
    private Integer buildUnitCount;
    // 필수 칼럼 제외한 데이터
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
