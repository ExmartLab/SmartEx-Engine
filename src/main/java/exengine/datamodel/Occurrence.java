package exengine.datamodel;

public enum Occurrence {
	FIRST("first"), SECOND("second"), MORE("more");

	private String occurrence;

	private Occurrence(String occurrence) {
		this.occurrence = occurrence;
	}

	public String toString() {
		return occurrence;
	}
}
