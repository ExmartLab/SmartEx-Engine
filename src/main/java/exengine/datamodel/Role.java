package exengine.datamodel;

public enum Role {
	OWNER("owner"), COWORKER("coworker"), GUEST("guest");

	private String role;

	private Role(String role) {
		this.role = role;
	}

	public String toString() {
		return role;
	}
}