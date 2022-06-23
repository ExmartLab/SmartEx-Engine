package exengine.database;

import org.springframework.data.annotation.Id;

public class Rule {
	  @Id
	  public String id;

	  public String ruleName;
	  public Trigger trigger;
	  public String action;

	  public Rule() {}

	  public Rule(String ruleName, Trigger trigger, String action) {
	    this.ruleName = ruleName;
	    this.trigger = trigger;
	    this.action = action;
	  }

	  @Override
	  public String toString() {
	    return String.format(
	        "Path[id=%s, name='%s', trigger='%s', action='%s']",
	        id, ruleName, trigger.toString(), action);
	  }
}
