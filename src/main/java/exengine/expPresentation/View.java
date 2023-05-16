package exengine.expPresentation;

public enum View {
	FULLEX(4), FACTEX(3), RULEEX(2), SIMPLDEX(1), ERRFULLEX(3), ERRSOLEX(2), ERROREX(1);

	private int value;

	private View(int value) {
			this.value = value;
		}

	public int getValue() {
		return value;
	}

}