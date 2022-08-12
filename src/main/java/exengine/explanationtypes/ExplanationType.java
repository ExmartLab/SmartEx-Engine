package exengine.explanationtypes;

public enum ExplanationType {
	FULLEX(4), FACTEX(3), RULEEX(2), SIMPLYDEX(1);

	private int type;

	private ExplanationType(int type) {
			this.type = type;
		}

	public int getValue() {
		return type;
	}

}