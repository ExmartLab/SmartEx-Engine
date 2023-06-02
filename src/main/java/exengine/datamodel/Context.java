package exengine.datamodel;

public class Context {

	private Role explaineeRole;
	private Occurrence occurrence;
	private Technicality explaineeTechnicality;
	private State explaineeState;
	private Integer explanationType;
	private String explaineeName;
	private String ownerName;

	public Context(Role explaineeRole, Occurrence occurrence, Technicality explaineeTechnicality, State explaineeState,
			Integer theExpType) {
		setExplaineeRole(explaineeRole);
		setOccurrence(occurrence);
		setExplaineeTechnicality(explaineeTechnicality);
		setExplaineeState(explaineeState);
		setExplanationType(theExpType);
	}

	/**
	 * Constructor for creating context by context service
	 * 
	 * @param explaineeRole
	 * @param occurrence
	 * @param explaineeTechnicality
	 * @param explaineeState
	 * @param explaineeName
	 * @param ownerName
	 */
	public Context(Role explaineeRole, Occurrence occurrence, Technicality explaineeTechnicality, State explaineeState,
			String explaineeName, String ownerName) {
		setExplaineeRole(explaineeRole);
		setOccurrence(occurrence);
		setExplaineeTechnicality(explaineeTechnicality);
		setExplaineeState(explaineeState);
		setExplaineeName(explaineeName);
		setOwnerName(ownerName);
	}

	public Role getExplaineeRole() {
		return explaineeRole;
	}

	public void seExplaineetRole(Role role) {
		this.explaineeRole = role;
	}

	public Occurrence getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(Occurrence occurrence) {
		this.occurrence = occurrence;
	}

	public Technicality getExplaineeTechnicality() {
		return explaineeTechnicality;
	}

	public void setExplaineeTechnicality(Technicality technicality) {
		this.explaineeTechnicality = technicality;
	}

	public State getExplaineeState() {
		return explaineeState;
	}

	public void setExplaineeState(State state) {
		this.explaineeState = state;
	}

	public Integer getExplanationType() {
		return explanationType;
	}

	public void setExplanationType(Integer theExpType) {
		this.explanationType = theExpType;
	}

	public String getExplaineeName() {
		return explaineeName;
	}

	public void setExplaineeName(String explaineeName) {
		this.explaineeName = explaineeName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownername) {
		this.ownerName = ownername;
	}

	public void setExplaineeRole(Role explaineeRole) {
		this.explaineeRole = explaineeRole;
	}

}
