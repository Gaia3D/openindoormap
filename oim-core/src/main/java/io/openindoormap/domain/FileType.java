package io.openindoormap.domain;

public enum FileType {

	DIRECTORY("D"), FILE("F");
	
	private final String value;
	
	FileType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}	
}
