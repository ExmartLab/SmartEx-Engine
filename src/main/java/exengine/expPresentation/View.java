package exengine.expPresentation;

/**
 * The View enum represents different types of explanation represesenations as
 * defined in the paper.
 */
public enum View {
	FULLEX(4), FACTEX(3), RULEEX(2), SIMPLDEX(1), ERRFULLEX(3), ERRSOLEX(2), ERROREX(1);

	private int value;

	/**
	 * Constructs a View enum constant with the specified value.
	 *
	 * @param value the priority value associated with the view
	 */
	private View(int value) {
		this.value = value;
	}

	/**
	 * Returns the integer value associated with the view.
	 *
	 * @return the priority value of the view
	 */
	public int getValue() {
		return value;
	}

}