package exengine.explanationtypes;

public enum ExplanationTypes {
	FULLEX("full explanation"), FACTEX("fact explanation"), RULEEX("rule explanation"), RESTRICTEDEX("restricted explanation");

	private String type;

	private ExplanationTypes(String type) {
			this.type = type;
		}

	public String toString() {
		return type;
	}

}
