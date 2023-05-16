package exengine.datamodel;

import org.springframework.data.annotation.Id;

public class User {

	@Id
	public String id;

	private String name;
	private int age;
	private Role role;
	private Technicality technicality;
	private State state;
	private String location;

	private String stateString = "null";

	private String userid;

	public User() {
		// default state
		this.setState(State.BREAK);
	}

	public User(String name, String userid, Role role, Technicality technicality) {
		this.userid = userid;
		this.name = name;
		this.role = role;
		this.technicality = technicality;

		// default state
		this.setState(State.BREAK);
	}

	public User(String name, int age, Role role, Technicality technicality, State state, String location) {
		this.name = name;
		this.age = age;
		this.role = role;
		this.technicality = technicality;
		this.state = state;
		stateString = state.toString();
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("User[id=%s, userId=%s, name=%s, role=%s, expertise=%s]", id, userid, name,
				role.toString(), technicality.toString());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
		stateString = state.toString();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStateString() {
		return stateString;
	}

	public void setStateString(String stateString) {
		this.stateString = stateString;
	}

	public String getUserId() {
		return userid;
	}

	public void setUserId(String userid) {
		this.userid = userid;
	}
}