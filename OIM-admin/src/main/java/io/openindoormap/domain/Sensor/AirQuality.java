package io.openindoormap.domain.Sensor;

public enum AirQuality {
    PM10("미세먼지(PM10)"),
    PM25("미세먼지(PM2.5)"),
    SO2("아황산가스 농도"),
    CO("일산화탄소 농도"),
    O3("오존 농도"),
    NO2("이산화질소 농도");

    private final String value;

    AirQuality(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AirQuality findBy(String value) {
        for(AirQuality type : values()) {
            if(type.getValue().equals(value)) return type;
        }
        return null;
    }
}
