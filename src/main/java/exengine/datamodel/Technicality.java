package exengine.datamodel;

public enum Technicality {
	TECHNICAL("technical"), MEDTECH("medium technical"), NONTECH("non technical");

	private String technicalityString;

	private Technicality(String technicality) {
		this.technicalityString = technicality;
	}

	public String getString() {
		return technicalityString;
	}
}
