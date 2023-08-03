package exengine.datamodel;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;

/**
 * The Rule class represents an automation rule in Home Assistant.
 */
public class Rule {

	@Id
	private String id; // only for MongoDB
	private String ruleName;
	private ArrayList<LogEntry> trigger;
	private ArrayList<LogEntry> conditions;
	private ArrayList<LogEntry> actions;
	private String ownerId;
	private String ruleDescription;
	private String ruleId;

	/**
	 * Constructs a new Rule object with the specified parameters.
	 *
	 * @param ruleName        the name of the rule
	 * @param ruleId          the ID of the rule
	 * @param trigger         the list of trigger log entries (Trigger also in Home
	 *                        Assistant)
	 * @param conditions      the list of conditions for the rule (Conditions in
	 *                        Home Assistant)
	 * @param actions         the list of actions to be performed when this rule is
	 *                        fired
	 * @param ownerId         the ID of the rule's owner
	 * @param ruleDescription the description of the rule
	 */
	public Rule(String ruleName, String ruleId, ArrayList<LogEntry> trigger, ArrayList<LogEntry> conditions,
			ArrayList<LogEntry> actions, String ownerId, String ruleDescription) {
		setRuleName(ruleName);
		setRuleId(ruleId);
		setTrigger(trigger);
		setConditions(conditions);
		setActions(actions);
		setOwnerId(ownerId);
		setRuleDescription(ruleDescription);
	}

	/**
	 * Returns the name of the rule.
	 *
	 * @return the rule name
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * Sets the name of the rule.
	 *
	 * @param ruleName the rule name to set
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * Returns the list of trigger log entries for the rule.
	 *
	 * @return the list of trigger log entries (empty if no trigger exists)
	 */
	public ArrayList<LogEntry> getTrigger() {
		if (trigger != null) {
			return trigger;			
		} else {
			return new ArrayList<>();			
		}
	}

	/**
	 * Sets the list of trigger log entries for the rule.
	 *
	 * @param trigger the list of trigger log entries to set
	 */
	public void setTrigger(ArrayList<LogEntry> trigger) {
		this.trigger = trigger;
	}

	/**
	 * Returns the list of actions to be performed by the rule.
	 *
	 * @return the list of actions
	 */
	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	/**
	 * Sets the list of actions to be performed by the rule.
	 *
	 * @param actions the list of actions to set
	 */
	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	/**
	 * Returns the list of conditions for the rule.
	 *
	 * @return the list of conditions
	 */
	public ArrayList<LogEntry> getConditions() {
		return conditions;
	}

	/**
	 * Sets the list of conditions for the rule.
	 *
	 * @param conditions the list of conditions to set
	 */
	public void setConditions(ArrayList<LogEntry> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Returns the ID of the rule's owner.
	 *
	 * @return the owner ID
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * Sets the ID of the rule's owner.
	 *
	 * @param ownerId the owner ID to set
	 */
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Returns the description of the rule.
	 * 
	 * @return the rule description
	 */
	public String getRuleDescription() {
		return ruleDescription;
	}

	/**
	 * Sets the description of the rule.
	 * 
	 * @param ruleDescription the rule description to set
	 */
	public void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}

	/**
	 * Returns the ID of the rule.
	 * 
	 * @return the rule ID
	 */
	public String getRuleId() {
		return ruleId;
	}

	/**
	 * Sets the ID of the rule.
	 * 
	 * @param ruleId the rule ID to set
	 */
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
}
