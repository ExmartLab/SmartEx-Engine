package exengine.datamodel;

import org.springframework.data.annotation.Id;

public class User {

	@Id
	public String id;

	private int userId;
	private String name;
	private int age;
	private Role role;
	private Technicality technicality;
	private State state;
	private String location;

	public User() {
	}

	public User(int userId, String name, int age, Role role, Technicality technicality, State state, String location) {
		this.userId = userId;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Technicality getTechnicality() {
		return technicality;
	}

	public void setTechnicality(Technicality technicality) {
		this.technicality = technicality;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}