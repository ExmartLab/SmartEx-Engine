package exengine.expPresentation;

public enum ExplanationType {
	FULLEX(4), FACTEX(3), RULEEX(2), SIMPLDEX(1);

	private int type;

	private ExplanationType(int type) {
			this.type = type;
		}

	public int getValue() {
		return type;
	}

}