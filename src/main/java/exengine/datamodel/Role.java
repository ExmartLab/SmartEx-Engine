package exengine.datamodel;

public enum Role {
	OWNER("owner"), COWORKER("coworker"), GUEST("guest");

	private String roleString;

	private Role(String role) {
		this.roleString = role;
	}

	public String getString() {
		return roleString;
	}
}