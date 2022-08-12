package exengine.contextmanagement;

import java.util.List;

import exengine.datamodel.*;
import exengine.explanationgenerationservice.Cause;

public class ContextManager {

	public static Context getAllContext(Cause cause, List<Rule> dbRules, List<User> dbUsers, String explaineeId) {
		
		//find owner id from dbRules
		String ruleOwnerId = null;
		for(Rule rule : dbRules) {
			if(rule.ruleName.equals(cause.getRule())) {
				ruleOwnerId = rule.getOwnerId();
			}
		}
		
		//find explainee and ruleOwner in User List
		User explainee = null;
		User ruleOwner = null;
		for (User user : dbUsers) {
			if(user.id.equals(explaineeId)) {
				explainee = user;
			}
			if(user.id.equals(ruleOwnerId)) {
				ruleOwner = user;
			}
		}
		
		
		
		//Context context = new Context();
		//return context;
		return null;
	}
	
	/*
	 * USER CONTEXT
	 * 
	 * EXPLAINEE (+ ROLE)
	 * 
	 * RULE + OWNER (from db)
	 * 
	 * GETTER & SETTER
	 */
	
}
