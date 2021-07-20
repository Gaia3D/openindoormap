package io.openindoormap.sensor;

import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;

/**
 * 미세먼지 datastream
 */
public enum AirQualityDatastream {
    PM10("미세먼지(PM10)", "microgram per cubic meter"),
    PM25("미세먼지(PM2.5)", "microgram per cubic meter"),
    SO2("아황산가스 농도", "parts per million"),
    CO("일산화탄소 농도", "parts per million"),
    O3("오존 농도", "parts per million"),
    NO2("이산화질소 농도", "parts per million"),
    PM10Daily("미세먼지(PM10) 24시간", "microgram per cubic meter"),
    PM25Daily("미세먼지(PM2.5) 24시간", "microgram per cubic meter"),
    SO2Daily("아황산가스 농도 24시간", "parts per million"),
    CODaily("일산화탄소 농도 24시간", "parts per million"),
    O3Daily("오존 농도 24시간", "parts per million"),
    NO2Daily("이산화질소 농도 24시간", "parts per million");

    private final String name;
    private final String unitOfMeasurementName;

    AirQualityDatastream(String name, String unitOfMeasurementName) {
        this.name = name;
        this.unitOfMeasurementName = unitOfMeasurementName;
    }

    public String getName() {
        return name;
    }

    public UnitOfMeasurement getUnitOfMeasurement() {
        UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
        if (this.unitOfMeasurementName.equals("microgram per cubic meter")) {
            unitOfMeasurement.setName(this.unitOfMeasurementName);
            unitOfMeasurement.setSymbol("㎍/m³");
            unitOfMeasurement.setDefinition("https://www.eea.europa.eu/themes/air/air-quality/resources/glossary/g-m3");
            return unitOfMeasurement;
        }

        unitOfMeasurement.setName(this.unitOfMeasurementName);
        unitOfMeasurement.setSymbol("ppm");
        unitOfMeasurement.setDefinition("https://en.wikipedia.org/wiki/Parts-per_notation");

        return unitOfMeasurement;
    }
}
