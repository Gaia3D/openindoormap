package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignLayerBuilding {

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
    // 필수 칼럼 제외한 데이터
    private String properties;

    // 유닛 타입
    private String buildUnitType;
    // 유닛 타입이 사용된 개수
    private Integer buildUnitCount;

    // wkt
    private String theGeom;
    // 활성화 여부 'Y', 'N'
    private String enableYn;
    // shape 버전 아이디
    private Integer versionId;
    // 좌표계
    private Integer coordinate;

    // 수정일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
    // 등록일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
}
