package io.openindoormap.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class GdalContourCommandParams {
    public static final String GDAL_CONTOUR = "gdal_contour";
    public static final String OGRINFO = "ogrinfo";
    public static final String OUTPUT_FILE_NAME = "contour.shp";
    public static final String SPATIAL_INDEX_QUERY = "CREATE SPATIAL INDEX ON contour";

    private final String attributeName = "grade";
    private final double interval = 50.0;
}
