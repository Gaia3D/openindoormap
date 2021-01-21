package io.openindoormap.utils.airquality;

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
        for (AirQuality air : AirQuality.values()) {
            if (air.getName().equals(name)) {
                return air;
            }
        }
        return null;
    }
}
