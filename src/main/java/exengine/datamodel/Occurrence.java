package exengine.datamodel;

/**
 * The Occurrence enum represents different occurrences (i.e., how often an
 * explanation was generated, i.e., a count encoded as strings). Each occurrence
 * has an associated string representation.
 */
public enum Occurrence {
	FIRST("first"), SECOND("second"), MORE("more");

	private String occurrenceString;

	/**
	 * Constructs an Occurrence enum constant with the specified string
	 * representation.
	 *
	 * @param occurrence the string representation of the occurrence
	 */
	private Occurrence(String occurrence) {
		this.occurrenceString = occurrence;
	}

	/**
	 * Returns the string representation of the occurrence.
	 *
	 * @return the string representation of the occurrence
	 */
	public String getString() {
		return occurrenceString;
	}
}
