package io.openindoormap.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geojson.Crs;
import org.geojson.jackson.CrsType;

public class GeoJsonUtils {
	public static final String CRS_URN_BASE_EPSG = "urn:ogc:def:crs:EPSG:8.8.1:";

	public static Crs createCrsForEpsgCode(int epsgCode) {
		Crs crs = new Crs();
		crs.setType(CrsType.name);
		String crsURN = CRS_URN_BASE_EPSG + epsgCode;
		crs.setProperties(Collections.unmodifiableMap(Stream.of(new SimpleEntry<>("name", crsURN))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue))));
		return crs;
	}
}
