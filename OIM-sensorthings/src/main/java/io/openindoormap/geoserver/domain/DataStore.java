package io.openindoormap.geoserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("dataStore")
public class DataStore {
    private String name;
    private String description;
    private boolean enabled;
    @JsonProperty("workspace")
    private WorkSpace workspace;
    private ConnectionParameters connectionParameters;
}
