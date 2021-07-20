package io.openindoormap.geoserver.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectionParameters {
    private List<ConnectionParameter> entry;
}