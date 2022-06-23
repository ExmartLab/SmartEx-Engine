package exengine.database;

import org.springframework.data.annotation.Id;

public class User {
	  @Id
	  public String id;

	  public String name;
	  public String role;
	  public String location;

	  public User() {}

	  public User(String name, String role, String location) {
	    this.name = name;
	    this.role = role;
	    this.location = location;
	  }

	  @Override
	  public String toString() {
	    return String.format(
	        "Path[id=%s, name='%s', trigger='%s', action='%s']",
	        id, name, role, location);
	  }
	
}
