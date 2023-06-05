package exengine.datamodel;

import org.springframework.data.annotation.Id;

/**
 * The User class represents a user of the smart home system. It contains
 * information about the user's name, age, role, technicality, state, location,
 * and user ID.
 */
public class User {

	@Id
	private String id;

	private String name;
	private int age;
	private Role role;
	private Technicality technicality;
	private State state;
	private String location;
	private String stateString;
	private String userid;

	/**
	 * Constructs a new User object with default values. The default state is set to
	 * State.BREAK.
	 */
	public User() {
		this.setState(State.BREAK); // default state
	}

	/**
	 * Constructs a new User object with the specified parameters. The default state
	 * is set to State.BREAK.
	 *
	 * @param name         the name of the user
	 * @param userId       the ID of the user
	 * @param role         the role of the user
	 * @param technicality the technicality level of the user
	 */
	public User(String name, String userId, Role role, Technicality technicality) {
		setUserId(userId);
		setName(name);
		setRole(role);
		setTechnicality(technicality);
		setState(State.BREAK); // default state
	}

	/**
	 * Constructs a new User object with the specified name, age, role,
	 * technicality, state, and location.
	 *
	 * @param name         the name of the user
	 * @param age          the age of the user
	 * @param role         the role of the user
	 * @param technicality the technicality level of the user
	 * @param state        the state of the user
	 * @param location     the location of the user
	 */
	public User(String name, int age, Role role, Technicality technicality, State state, String location) {
		setName(name);
		setAge(age);
		setRole(role);
		setTechnicality(technicality);
		setState(state);
		setLocation(location);
	}

	/**
	 * Returns the ID of the user.
	 *
	 * @return the ID of the user
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the ID of the user.
	 *
	 * @param id the ID of the user
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the name of the user.
	 *
	 * @return the name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the user.
	 *
	 * @param name the name of the user
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the age of the user.
	 *
	 * @return the age of the user
	 */
	public int getAge() {
		return age;
	}

	/**
	 * Sets the age of the user.
	 *
	 * @param age the age of the user
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * Returns the role of the user.
	 *
	 * @return the role of the user
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Sets the role of the user.
	 *
	 * @param role the role of the user
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Returns the technicality level of the user.
	 *
	 * @return the technicality level of the user
	 */
	public Technicality getTechnicality() {
		return technicality;
	}

	/**
	 * Sets the technicality level of the user.
	 *
	 * @param technicality the technicality level of the user
	 */
	public void setTechnicality(Technicality technicality) {
		this.technicality = technicality;
	}

	/**
	 * Returns the state of the user.
	 *
	 * @return the state of the user
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets the state of the user.
	 *
	 * @param state the state of the user
	 */
	public void setState(State state) {
		this.state = state;
		setStateString(state);
	}

	/**
	 * Returns the location of the user.
	 *
	 * @return the location of the user
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location of the user.
	 *
	 * @param location the location of the user
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Returns the string representation of the user's state.
	 *
	 * @return the string representation of the user's state
	 */
	public String getStateString() {
		return stateString;
	}

	/**
	 * Sets the string representation of the user's state.
	 *
	 * @param state the state of the user
	 */
	public void setStateString(State state) {
		this.stateString = state.toString();
	}

	/**
	 * Returns the user ID.
	 *
	 * @return the user ID
	 */
	public String getUserId() {
		return userid;
	}

	/**
	 * Sets the user ID.
	 *
	 * @param userid the user ID
	 */
	public void setUserId(String userid) {
		this.userid = userid;
	}
}