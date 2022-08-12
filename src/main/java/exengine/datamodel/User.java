package exengine.datamodel;

import org.springframework.data.annotation.Id;

public class User {

	@Id
	public String id;

	public String name;
	public int age;
	public Role role;
	public Technicality technicality;
	public State state;
	public String location;

	public User() {
	}

	public User(String name, int age, Role role, Technicality technicality, State state, String location) {
		this.name = name;
		this.age = age;
		this.role = role;
		this.technicality = technicality;
		this.state = state;
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("Customer[id=%s, name=%s, age=%d, role=%s, expertise=%d, state=%s, location=%s]", id, name,
				age, role.toString(), technicality.toString(), state.toString(), location);
	}
}