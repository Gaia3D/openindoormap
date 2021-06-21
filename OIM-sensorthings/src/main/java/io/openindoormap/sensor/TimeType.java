package io.openindoormap.sensor;

public enum TimeType {
    HOUR("HOUR"),
    DAILY("DAILY");

    private final String value;

    TimeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
