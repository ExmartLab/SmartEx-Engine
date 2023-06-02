package exengine.datamodel;

public enum Occurrence {
	FIRST("first"), SECOND("second"), MORE("more");

	private String occurrenceString;

	private Occurrence(String occurrence) {
		this.occurrenceString = occurrence;
	}

	public String getString() {
		return occurrenceString;
	}
}
