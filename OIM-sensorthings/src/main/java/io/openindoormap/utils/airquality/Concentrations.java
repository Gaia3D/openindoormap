package io.openindoormap.utils.airquality;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Concentrations {
    private List<Concentration> concentrations;

    public Concentrations() {
        this.concentrations = new ArrayList<>();
    }

    public void addConcentration(Concentration concentration) {
        this.concentrations.add(concentration);
    }

    public Concentration getConcentration(AirQuality airQuality) {
        return concentrations.stream()
                .filter(concentration -> concentration.getAirQuality().equals(airQuality))
                .findFirst()
                .orElse(null);
    }
}
