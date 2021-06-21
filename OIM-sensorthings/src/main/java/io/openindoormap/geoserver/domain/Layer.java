package io.openindoormap.geoserver.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Layer {
    private String name;
    private String path;
    private String type;
    private DefaultStyle defaultStyle;
}
