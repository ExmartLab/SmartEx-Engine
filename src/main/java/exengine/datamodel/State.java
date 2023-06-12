package exengine.datamodel;

/**
 * The State enum represents different states of a user. Each state has an
 * associated string representation.
 */
public enum State {
	WORKING("working"), MEETING("meeting"), BREAK("break");

	private String stateString;

	/**
	 * Constructs a State enum constant with the specified string representation.
	 *
	 * @param state the string representation of the state
	 */
	private State(String state) {
		this.stateString = state;
	}

	/**
	 * Returns the string representation of the state.
	 *
	 * @return the string representation of the state
	 */
	public String getString() {
		return stateString;
	}
}