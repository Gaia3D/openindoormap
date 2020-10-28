package io.openindoormap.domain;

public enum OrderBy {

    DESC("desc"),
    ASC("asc");

    private final String value;

    OrderBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
