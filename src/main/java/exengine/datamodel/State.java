package exengine.datamodel;

public enum State {
	WORKING("working"), MEETING("meeting"), BREAK("break");

	private String state;

	private State(String state) {
		this.state = state;
	}

	public String toString() {
		return state;
	}
}