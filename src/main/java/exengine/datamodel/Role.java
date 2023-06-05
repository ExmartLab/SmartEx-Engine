package exengine.datamodel;

/**
 * The Role enum represents different roles that a user can have. Each role has
 * an associated string representation.
 */
public enum Role {
	OWNER("owner"), COWORKER("coworker"), GUEST("guest");

	private String roleString;

	/**
	 * Constructs a Role enum constant with the specified string representation.
	 *
	 * @param role the string representation of the role
	 */
	private Role(String role) {
		this.roleString = role;
	}

	/**
	 * Returns the string representation of the role.
	 *
	 * @return the string representation of the role
	 */
	public String getString() {
		return roleString;
	}
}