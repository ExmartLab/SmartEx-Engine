package exengine.datamodel;

public class Context {

	private Role explaineeRole;
	private Occurrence occurrence;
	private Technicality explaineeTechnicality;
	private State explaineeState;
	private Integer theExpType;

	private String explaineeName;
	private String ownerName;

	public Context(Role explaineeRole, Occurrence occurrence, Technicality explaineeTechnicality, State explaineeState,
			Integer theExpType) {
		this.explaineeRole = explaineeRole;
		this.occurrence = occurrence;
		this.explaineeTechnicality = explaineeTechnicality;
		this.explaineeState = explaineeState;
		this.theExpType = theExpType;
	}

	// constructor for creating context by context service
	public Context(Role explaineeRole, Occurrence occurrence, Technicality explaineeTechnicality, State explaineeState,
			String explaineeName, String ownerName) {
		this.explaineeRole = explaineeRole;
		this.occurrence = occurrence;
		this.explaineeTechnicality = explaineeTechnicality;
		this.explaineeState = explaineeState;
		this.explaineeName = explaineeName;
		this.ownerName = ownerName;
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

	public Integer getTheExpType() {
		return theExpType;
	}

	public void setTheExpType(Integer theExpType) {
		this.theExpType = theExpType;
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
