package io.openindoormap.utils.airquality;

import java.util.ArrayList;
import java.util.List;

public class Concentration {
    private AirQuality airQuality;
    private List<IndexStep> indexSteps;

    public Concentration(AirQuality airQuality) {
        this.airQuality = airQuality;
        this.indexSteps = new ArrayList<>();
    }

    public AirQuality getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(AirQuality airQuality) {
        this.airQuality = airQuality;
    }

    public List<IndexStep> getIndexSteps() {
        return indexSteps;
    }

    public void setIndexSteps(List<IndexStep> indexSteps) {
        this.indexSteps = indexSteps;
    }

	public void addIndexStep(IndexStep indexStep) {
        this.indexSteps.add(indexStep);
    }
    
    public IndexStep getIndexStep(double value) {
        for (IndexStep indexStep : indexSteps) {
            if (value >= indexStep.getMin() && value <= indexStep.getMax()) {
                return indexStep;
            }
        }

        return null;
    }
}
