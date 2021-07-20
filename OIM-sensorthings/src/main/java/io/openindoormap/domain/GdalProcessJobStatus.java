package io.openindoormap.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum GdalProcessJobStatus {

    // 성공
    SUCCESS("success"),
    // 실패
    FAIL("fail");

    private final @Getter String value;
    GdalProcessJobStatus(String value) {
        this.value = value;
    }

    public static Map<String, Object> toEnumHashMap() {
        Map<String, Object> eMap = new HashMap<>();
        Stream.of(GdalProcessJobStatus.values())
                .forEach(e ->  eMap.put(e.toString(), 0L));
        return eMap;
    }

    public static GdalProcessJobStatus findByStatus(String value) {
        return Arrays.stream(GdalProcessJobStatus.values())
                .filter(e -> e.value.equals(value))
                .findAny()
                .orElse(null);
    }

}
