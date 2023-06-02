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
	private String stateString;
	private String userid;

	public User() {
		this.setState(State.BREAK); // default state
	}

	public User(String name, String userId, Role role, Technicality technicality) {		
		setUserId(userId);
		setName(name);
		setRole(role);
		setTechnicality(technicality);
		setState(State.BREAK); // default state
	}

	public User(String name, int age, Role role, Technicality technicality, State state, String location) {
		setName(name);
		setAge(age);
		setRole(role);
		setTechnicality(technicality);
		setState(state);
		setLocation(location);
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
		setStateString(state);
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

	public void setStateString(State state) {
		this.stateString = state.toString();
	}

	public String getUserId() {
		return userid;
	}

	public void setUserId(String userid) {
		this.userid = userid;
	}
}