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
public class DesignLayerBuildingHeightDto {

    // design layer building 고유번호
    private Long designLayerBuildingHeightId;

    // design layer 고유 번호
    private Long designLayerId;
    // design layer 그룹 고유 번호
    private Integer designLayerGroupId;

    // shape 파일 고유 번호
    private Long buildId;

    // 최고 층수
    private Integer buildMaximumFloors;

    // 필수 칼럼 제외한 데이터
    private String properties;
    // wkt
    private String theGeom;
    // 활성화 여부 'Y', 'N'
    private String enableYn;
    // shape 버전 아이디
    private Integer versionId;

    // 수정일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
    // 등록일
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
}
