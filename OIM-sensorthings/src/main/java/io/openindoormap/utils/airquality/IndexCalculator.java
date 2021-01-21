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

        double rawIndex = (iHigh - iLow) / (cHigh - cLow) * (rawConcentration - cLow) + iLow;

        return (int) Math.round(rawIndex);
    }

	public int getAQI(AirQuality airQuality, double value) {
        int index = INDEX_MISSING;

        Concentration concentration = concentrations.getConcentration(airQuality);
        if(concentration != null) {
            IndexStep IndexStep = concentration.getIndexStep(value);
            if(IndexStep != null)
            {
                index = calculateIndex(value, IndexStep);
            }
        }

		return index;
    }
}
