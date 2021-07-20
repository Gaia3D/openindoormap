package io.openindoormap.utils.airquality;

import java.util.Arrays;

public enum AirQuality {
    PM10("PM10"),
    PM25("PM2.5"),
    SO2("SO2"),
    CO("CO"),
    O3("O3"),
    NO2("NO2");

    private final String name;

    AirQuality(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AirQuality parseFromString(String name) {
        return Arrays.stream(AirQuality.values())
                .filter(air -> air.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
