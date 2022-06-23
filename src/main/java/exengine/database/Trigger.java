package exengine.database;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Trigger {
	  @Id
	  public String id;

	  public List<String> conditions;

	  public Trigger() {}

	  public Trigger(List<String> conditions) {
	    this.conditions = conditions;
	  }

	  @Override
	  public String toString() {
	    return conditions.toString();
	  }
}
