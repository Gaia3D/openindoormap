package io.openindoormap.domain.common;

import lombok.*;

import javax.validation.constraints.Max;
import java.util.List;

/**
 * 공간 연산을 위한 정보
 */
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpatialOperationInfo {

    // wkt s
    private String wkt;
    // land 필지, building 빌딩
    private String type;
    // 버퍼 정보 1 : 111km. 버퍼 사이즈는 좌표계마다 조금씩 다름
    @Max(1)
    private Float buffer;
    // 리턴 최대 갯수
    private Integer maxFeatures;
    // 좌표 리스트
    private List<GeometryInfo> geometryInfo;
}
