package io.openindoormap.sensor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 미세먼지 ObservedProperty 속성값 enum
 */
public enum AirQualityObservedProperty {
    PM10("pm10Value", "미세먼지(PM10) Particulates", "https://en.wikipedia.org/wiki/Particulates", TimeType.HOUR),
    PM25("pm25Value", "미세먼지(PM2.5) Particulates", "https://en.wikipedia.org/wiki/Particulates", TimeType.HOUR),
    SO2("so2Value", "아황산가스 농도 Sulfur_dioxide", "https://en.wikipedia.org/wiki/Sulfur_dioxide", TimeType.HOUR),
    CO("coValue", "일산화탄소 농도 Carbon_monoxide", "https://en.wikipedia.org/wiki/Carbon_monoxide", TimeType.HOUR),
    O3("o3Value", "오존 농도 Ozone", "https://en.wikipedia.org/wiki/Ozone", TimeType.HOUR),
    NO2("no2Value", "이산화질소 Nitrogen_dioxide", "https://en.wikipedia.org/wiki/Nitrogen_dioxide", TimeType.HOUR),
    PM10Daily("pm10ValueDaily", "미세먼지(PM10) Particulates", "https://en.wikipedia.org/wiki/Particulates", TimeType.DAILY),
    PM25Daily("pm25ValueDaily", "미세먼지(PM2.5) Particulates", "https://en.wikipedia.org/wiki/Particulates", TimeType.DAILY),
    SO2Daily("so2ValueDaily", "아황산가스 농도 Sulfur_dioxide", "https://en.wikipedia.org/wiki/Sulfur_dioxide", TimeType.DAILY),
    CODaily("coValueDaily", "일산화탄소 농도 Carbon_monoxide", "https://en.wikipedia.org/wiki/Carbon_monoxide", TimeType.DAILY),
    O3Daily("o3ValueDaily", "오존 농도 Ozone", "https://en.wikipedia.org/wiki/Ozone", TimeType.DAILY),
    NO2Daily("no2ValueDaily", "이산화질소 Nitrogen_dioxide", "https://en.wikipedia.org/wiki/Nitrogen_dioxide", TimeType.DAILY);

    private final String name;
    private final String description;
    private final String definition;
    private final TimeType type;

    AirQualityObservedProperty(String name, String description, String definition, TimeType type) {
        this.name = name;
        this.description = description;
        this.definition = definition;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefinition() {
        return definition;
    }

    public TimeType getTimeType() {
        return type;
    }

    public static List<AirQualityObservedProperty> getObservedPropertyByType(TimeType type) {
        return Arrays.stream(AirQualityObservedProperty.values())
                .filter(f -> f.type == type)
                .collect(Collectors.toList());
    }
}
