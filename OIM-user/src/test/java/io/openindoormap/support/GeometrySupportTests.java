package io.openindoormap.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import io.openindoormap.domain.common.GeometryInfo;
import io.openindoormap.support.GeometrySupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class GeometrySupportTests {

    @Test
    void test() {
        List<GeometryInfo> point = Collections.singletonList(new GeometryInfo(12.3133, 313.44432));

        List<GeometryInfo> line = Arrays.asList(
                new GeometryInfo(12.3133,313.44432),
                new GeometryInfo(31.35634,12.1212),
                new GeometryInfo(2121.212,123.13131),
                new GeometryInfo(128.21212,32.1212),
                new GeometryInfo(2121.212,123.13131),
                new GeometryInfo(11.122121,85.1212212));

        List<GeometryInfo> polygon = Arrays.asList(
                new GeometryInfo(12.3133,313.44432),
                new GeometryInfo(2121.212,123.13131),
                new GeometryInfo(128.21212,32.1212),
                new GeometryInfo(2121.212,123.13131),
                new GeometryInfo(12.3133,313.44432));
        log.info("point ============= {} ", GeometrySupport.toWKT(point));
        log.info("line ==============={} ", GeometrySupport.toWKT(line));
        log.info("polygon ============== {} ", GeometrySupport.toWKT(polygon));
        assertTrue(GeometrySupport.toWKT(point).startsWith("POINT"));
        assertTrue(GeometrySupport.toWKT(line).startsWith("LINE"));
        assertTrue(GeometrySupport.toWKT(polygon).startsWith("POLYGON"));

    }
}