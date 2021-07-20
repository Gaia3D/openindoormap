package io.openindoormap.domain.common;

import lombok.*;

/**
 * 포인트 정보
 */
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
public class GeometryInfo {
    // 경도
    private Double longitude;
    // 위도
    private Double latitude;
    // 높이
    private Double altitude;

    public GeometryInfo(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeometryInfo(Double longitude, Double latitude, Double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
}
