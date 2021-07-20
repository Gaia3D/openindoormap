package io.openindoormap.geoserver.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkSpace {
    private final String name;
    private String link;
}
