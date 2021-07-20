package io.openindoormap.utils.airquality;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Concentration {
    private AirQuality airQuality;
    private List<IndexStep> indexSteps;

    public Concentration(AirQuality airQuality) {
        this.airQuality = airQuality;
        this.indexSteps = new ArrayList<>();
    }

    public void addIndexStep(IndexStep indexStep) {
        this.indexSteps.add(indexStep);
    }

    public IndexStep getIndexStep(double value) {
        IndexStep maxIndexStep = getMaxIndexStep();
        if (maxIndexStep.getMax() < value) {
            return maxIndexStep;
        }
        return indexSteps.stream()
                .filter(indexStep -> value >= indexStep.getMin() && value <= indexStep.getMax())
                .findFirst()
                .orElse(null);
    }

    private IndexStep getMaxIndexStep() {
        return indexSteps.stream()
                .sorted()
                .skip(indexSteps.size() - 1)
                .findFirst()
                .orElse(null);
    }

}
