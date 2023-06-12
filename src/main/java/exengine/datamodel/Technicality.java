package exengine.datamodel;

/**
 * The Technicality enum represents different levels of technicality. Each level
 * has an associated string representation.
 */
public enum Technicality {
	TECHNICAL("technical"), MEDTECH("medium technical"), NONTECH("non technical");

	private String technicalityString;

	/**
	 * Constructs a Technicality enum constant with the specified string
	 * representation.
	 *
	 * @param technicality the string representation of the technicality level
	 */
	private Technicality(String technicality) {
		this.technicalityString = technicality;
	}

	/**
	 * Returns the string representation of the technicality level.
	 *
	 * @return the string representation of the technicality level
	 */
	public String getString() {
		return technicalityString;
	}
}
