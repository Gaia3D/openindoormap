package io.openindoormap.utils.airquality;

import java.util.ArrayList;
import java.util.List;

public class Concentrations {
    private List<Concentration> concentrations;

    public Concentrations() {
        this.concentrations = new ArrayList<>();
    }

    public List<Concentration> getConcentrations() {
        return concentrations;
    }

    public void setConcentrations(List<Concentration> concentrations) {
        this.concentrations = concentrations;
    }

    public void addConcentration(Concentration concentration) {
        this.concentrations.add(concentration);
    }

    public Concentration getConcentration(AirQuality airQuality) {
        for (Concentration concentration : concentrations) {
            if(concentration.getAirQuality().equals(airQuality)) {
                return concentration;
            }
        }

        return null;
    }
}
