package io.openindoormap.geoserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionParameter {
    @JsonProperty("@key")
    private String key;
    @JsonProperty("$")
    private String value;
}
