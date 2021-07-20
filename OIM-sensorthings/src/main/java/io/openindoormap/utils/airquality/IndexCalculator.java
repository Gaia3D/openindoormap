package io.openindoormap.utils.airquality;

public class IndexCalculator {

    private Concentrations concentrations;
    public static int INDEX_MISSING = -9999;

    public IndexCalculator(Concentrations concentrations) {
        this.concentrations = concentrations;
    }

    private int calculateIndex(double rawConcentration, IndexStep concentration) {
        int iLow = concentration.getIndex().getMin();
        int iHigh = concentration.getIndex().getMax();
        double cLow = concentration.getMin();
        double cHigh = concentration.getMax();

        double rawIndex = concentration.getIndex().getMax();
        if (rawConcentration <= cHigh) {
            rawIndex = (iHigh - iLow) / (cHigh - cLow) * (rawConcentration - cLow) + iLow;
        }

        return (int) Math.round(rawIndex);
    }

    public int getAQI(AirQuality airQuality, double value) {
        int index = INDEX_MISSING;
        IndexStep indexStep = getIndexStep(airQuality, value);
        if (indexStep != null) {
            index = calculateIndex(value, indexStep);
        }
        return index;
    }

    public int getGrade(AirQuality airQuality, double value) {
        int grade = 0;
        IndexStep indexStep = getIndexStep(airQuality, value);
        if (indexStep != null) {
            grade = indexStep.getIndex().getGrade();
        }
        return grade;
    }

    private IndexStep getIndexStep(AirQuality airQuality, double value) {
        IndexStep indexStep = null;
        Concentration concentration = concentrations.getConcentration(airQuality);
        if (concentration != null) {
            indexStep = concentration.getIndexStep(value);
        }
        return indexStep;
    }

}
