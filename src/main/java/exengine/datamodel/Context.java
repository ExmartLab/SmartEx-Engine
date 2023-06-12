package exengine.datamodel;

/**
 * The Context class represents and bundles the contextual information for
 * explaining a situation. It contains the explainee's role, occurrence,
 * technicality, state, explanation type, explainee's name, and owner's name.
 */
public class Context {

	private Role explaineeRole;
	private Occurrence occurrence;
	private Technicality explaineeTechnicality;
	private State explaineeState;
	private Integer explanationType;
	private String explaineeName;
	private String ownerName;

	/**
	 * Constructs a Context object with the specified explainee role, occurrence,
	 * explainee technicality, explainee state, and explanation type.
	 *
	 * @param explaineeRole         the role of the explainee
	 * @param occurrence            the occurrence associated with the context
	 * @param explaineeTechnicality the technicality of the explainee
	 * @param explaineeState        the state of the explainee
	 * @param explanationType       the type of explanation
	 */
	public Context(Role explaineeRole, Occurrence occurrence, Technicality explaineeTechnicality, State explaineeState,
			Integer theExpType) {
		setExplaineeRole(explaineeRole);
		setOccurrence(occurrence);
		setExplaineeTechnicality(explaineeTechnicality);
		setExplaineeState(explaineeState);
		setExplanationType(theExpType);
	}

	/**
	 * Constructs a Context object with the specified explainee role, occurrence,
	 * explainee technicality, explainee state, explainee name, and owner's name.
	 *
	 * @param explaineeRole         the role of the explainee
	 * @param occurrence            the occurrence associated with the context
	 * @param explaineeTechnicality the technicality of the explainee
	 * @param explaineeState        the state of the explainee
	 * @param explaineeName         the name of the explainee
	 * @param ownerName             the name of the owner
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

	/**
	 * Returns the role of the explainee.
	 *
	 * @return the role of the explainee
	 */
	public Role getExplaineeRole() {
		return explaineeRole;
	}

	/**
	 * Sets the role of the explainee.
	 *
	 * @param explaineeRole the role of the explainee
	 */
	public void setExplaineeRole(Role role) {
		this.explaineeRole = role;
	}

	/**
	 * Returns the occurrence information associated with the context.
	 *
	 * @return the occurrence information associated with the context
	 */
	public Occurrence getOccurrence() {
		return occurrence;
	}

	/**
	 * Sets the occurrence information associated with the context.
	 *
	 * @param occurrence the occurrence associated with the context
	 */
	public void setOccurrence(Occurrence occurrence) {
		this.occurrence = occurrence;
	}

	/**
	 * Returns the technicality of the explainee.
	 *
	 * @return the technicality of the explainee
	 */
	public Technicality getExplaineeTechnicality() {
		return explaineeTechnicality;
	}

	/**
	 * Sets the technicality of the explainee.
	 *
	 * @param explaineeTechnicality the technicality of the explainee
	 */
	public void setExplaineeTechnicality(Technicality technicality) {
		this.explaineeTechnicality = technicality;
	}

	/**
	 * Returns the state of the explainee.
	 *
	 * @return the state of the explainee
	 */
	public State getExplaineeState() {
		return explaineeState;
	}

	/**
	 * Sets the state of the explainee.
	 *
	 * @param explaineeState the state of the explainee
	 */
	public void setExplaineeState(State state) {
		this.explaineeState = state;
	}

	/**
	 * Returns the type of explanation.
	 *
	 * @return the type of explanation
	 */
	public Integer getExplanationType() {
		return explanationType;
	}

	/**
	 * Sets the type of explanation.
	 *
	 * @param explanationType the type of explanation
	 */
	public void setExplanationType(Integer theExpType) {
		this.explanationType = theExpType;
	}

	/**
	 * Returns the name of the explainee.
	 *
	 * @return the name of the explainee
	 */
	public String getExplaineeName() {
		return explaineeName;
	}

	/**
	 * Sets the name of the explainee.
	 *
	 * @param explaineeName the name of the explainee
	 */
	public void setExplaineeName(String explaineeName) {
		this.explaineeName = explaineeName;
	}

	/**
	 * Returns the name of the owner.
	 *
	 * @return the name of the owner
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * Sets the name of the owner.
	 *
	 * @param ownerName the name of the owner
	 */
	public void setOwnerName(String ownername) {
		this.ownerName = ownername;
	}

}
