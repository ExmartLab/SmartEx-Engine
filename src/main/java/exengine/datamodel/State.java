package exengine.datamodel;

public enum State {
	WORKING("working"), MEETING("meeting"), BREAK("break");

	private String stateString;

	private State(String state) {
		this.stateString = state;
	}

	public String getString() {
		return stateString;
	}
}