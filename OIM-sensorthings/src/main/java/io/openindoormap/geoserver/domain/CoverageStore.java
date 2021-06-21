package io.openindoormap.geoserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("coverageStore")
public class CoverageStore {
    private final String name;
    private String description;
    private final String type;
    private boolean enabled;
    @JsonProperty("workspace")
    private WorkSpace workspace;
    private String url;
}
