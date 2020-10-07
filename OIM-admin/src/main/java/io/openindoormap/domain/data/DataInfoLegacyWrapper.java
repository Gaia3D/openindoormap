package io.openindoormap.domain.data;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataInfoLegacyWrapper {
	private List<DataInfoLegacy> children; 
}