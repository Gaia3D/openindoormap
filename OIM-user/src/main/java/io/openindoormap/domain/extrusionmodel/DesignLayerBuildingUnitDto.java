package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 디자인 레이어 빌딩 유닛
 */
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignLayerBuildingUnitDto {

    // 디자인 레이어 빌딩 유닛 고유번호
    private Long designLayerBuildingId;
    // 도시 그룹 고유번호
    private Integer urbanGroupId;
    // 디자인 레이어 빌딩 유닛 타입(이름)
    private String unitType;

    // 전용 면적
    private BigDecimal dedicatedArea;
    // 전용 면적 평수
    private BigDecimal dedicatedAreaAcreage;
    // 주거 공용
    private BigDecimal residentialCommonArea;
    // 주거 공용 평수
    private BigDecimal residentialCommonAcreage;
    // 공급 면적
    private BigDecimal supplyArea;
    // 공급 면적 평수
    private BigDecimal supplyAcreage;
    // 기타 공용
    private BigDecimal otherCommonArea;
    // 기타 공용 평수
    private BigDecimal otherCommonAcreage;
    // 계약 면적
    private BigDecimal contactArea;
    // 계약 면적 평수
    private BigDecimal contactAcreage;
    // 발코니 확장
    private BigDecimal balconyExtensionArea;
    // 발코니 확장 평수
    private BigDecimal balconyExtensionAcreage;

    // 수정일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
    // 등록일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
}
