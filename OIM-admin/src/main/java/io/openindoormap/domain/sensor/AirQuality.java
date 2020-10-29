package io.openindoormap.domain.sensor;

public enum AirQuality {
    PM10("미세먼지(PM10)", "pm10Value"),
    PM25("미세먼지(PM2.5)", "pm25Value"),
    SO2("아황산가스 농도", "so2Value"),
    CO("일산화탄소 농도", "coValue"),
    O3("오존 농도", "o3Value"),
    NO2("이산화질소 농도", "no2Value");

    private final String datastreamName;
    private final String observedPropertyName;

    AirQuality(String datastreamName, String observedPropertyName) {
        this.datastreamName = datastreamName;
        this.observedPropertyName = observedPropertyName;
    }

    public String getDatastreamName() {
        return datastreamName;
    }

    public String getObservedPropertyName() {
        return observedPropertyName;
    }
}
