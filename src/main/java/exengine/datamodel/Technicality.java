package exengine.datamodel;

public enum Technicality {
	TECHNICAL("technical"), MEDTECH("medium technical"), NONTECH("non technical");

	private String technicality;

	private Technicality(String technicality) {
		this.technicality = technicality;
	}

	public String toString() {
		return technicality;
	}
}
