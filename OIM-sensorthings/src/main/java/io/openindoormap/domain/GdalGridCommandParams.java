package io.openindoormap.domain;

import io.openindoormap.sensor.AirQualityObservedProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class GdalGridCommandParams {

    public static final String GDAL_GRID = "gdal_grid";
    public static final String SENSORTHINGS_QUERY_FORMAT = "SELECT\n" +
            "    things.\\\"NAME\\\",\n" +
            "    locations.\\\"NAME\\\",\n" +
            "    locations.\\\"GEOM\\\" as geom,\n" +
            "    observations.\\\"RESULT_JSON\\\"->>'index' as grade,\n" +
            "    observations.\\\"RESULT_TIME\\\"\n" +
            "FROM\n" +
            "    \\\"THINGS\\\" things\n" +
            "    LEFT JOIN \\\"THINGS_LOCATIONS\\\" things_locations ON things.\\\"ID\\\" = things_locations.\\\"THING_ID\\\"\n" +
            "    LEFT JOIN \\\"LOCATIONS\\\" locations ON things_locations.\\\"LOCATION_ID\\\" = locations.\\\"ID\\\"\n" +
            "    LEFT JOIN \\\"DATASTREAMS\\\" datastreams ON things.\\\"ID\\\" = datastreams.\\\"THING_ID\\\"\n" +
            "    LEFT JOIN \\\"OBSERVATIONS\\\" observations ON datastreams.\\\"ID\\\" = observations.\\\"DATASTREAM_ID\\\"\n" +
            "    LEFT JOIN \\\"OBS_PROPERTIES\\\" obs_properties ON datastreams.\\\"OBS_PROPERTY_ID\\\" = obs_properties.\\\"ID\\\"\n" +
            "WHERE\n" +
            "    obs_properties.\\\"NAME\\\" = '%1$s'\n" +
            "    AND (observations.\\\"RESULT_TIME\\\" AT TIME ZONE 'UTC') = TO_TIMESTAMP('%2$s', 'YYYY-MM-DD HH24:MI')";
    public static final String PG_INFO_FORMAT = "host='%s' port='%s' dbname='%s' user='%s' password='%s'";
    public static final String MINX = "124.645669";
    public static final String MAXX = "130.904047";
    public static final String MINY = "38.566511";
    public static final String MAXY = "33.227749";
    public static final String OUT_SIZE = "10000";
    public static final String EPSG_4326 = "EPSG:4326";
    public static final String OUTPUT_FILE_NAME = "dust.tif";

    private final String utcDateTime;
    private final AirQualityObservedProperty observedProperty;
    private final String pgInfo;
    private final String field = "grade";
}
