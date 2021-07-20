package io.openindoormap.support;

import java.util.List;
import java.util.stream.Collectors;

import io.openindoormap.domain.GeometryType;
import io.openindoormap.domain.common.GeometryInfo;

public class GeometrySupport {

    public static String toWKT(List<GeometryInfo> geometryInfoList) {
        GeometryType type;
        StringBuilder result = new StringBuilder();
        GeometryInfo startPoint = geometryInfoList.get(0);
        GeometryInfo endPoint = geometryInfoList.get(geometryInfoList.size() - 1);
        if (geometryInfoList.size() == 1) {
            type = GeometryType.POINT;
        } else if (startPoint.getLongitude().equals(endPoint.getLongitude()) && startPoint.getLatitude().equals(endPoint.getLatitude())) {
            type = GeometryType.POLYGON;
        } else {
            type = GeometryType.LINE;
        }

        String geom = geometryInfoList.stream()
                .map(f -> f.getLongitude() + " " + f.getLatitude())
                .collect(Collectors.joining(","));

        switch (type) {
            case POINT:
                result.append("POINT (")
                        .append(geom)
                        .append(")");
                break;
            case LINE:
                result.append("LINESTRING (")
                        .append(geom)
                        .append(")");
                break;
            case POLYGON:
                result.append("POLYGON ((")
                        .append(geom)
                        .append("))");
                break;
        }

        return result.toString();
    }
}
