package exengine.database;

import org.springframework.data.annotation.Id;

public class Path {

	  @Id
	  public String id;

	  public String ruleName;
	  public String trigger;
	  public String action;

	  public Path() {}

	  public Path(String ruleName, String trigger, String action) {
	    this.ruleName = ruleName;
	    this.trigger = trigger;
	    this.action = action;
	  }

	  @Override
	  public String toString() {
	    return String.format(
	        "Path[id=%s, name='%s', trigger='%s', action='%s']",
	        id, ruleName, trigger, action);
	  }

	
}
