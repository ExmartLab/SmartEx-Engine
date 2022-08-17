package exengine.database;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.datamodel.Rule;
import exengine.datamodel.User;

@Service
public class DatabaseService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	RuleRepository ruleRepo;

	/*
	 * RULE OPERATIONS
	 */
	public List<Rule> findAllRules() {
		return ruleRepo.findAll();
	}
	
	public void saveNewRule(Rule rule) {
		ruleRepo.save(rule);
	}
	
	public void deleteAllRules() {
		ruleRepo.deleteAll();
	}

	public Rule findRuleByName(String ruleName) {
		return ruleRepo.findByName(ruleName);
	}

	/*
	 * USER OPERATIONS
	 */
	public User findUserByName(String userName) {
		return userRepo.findByName(userName);
	}

	public User findUserByUserId(int userId) {
		return userRepo.findByUserId(userId);
	}

	/*
	 * MISC.
	 */
	public User findOwnerByRuleName(String ruleName) {
		return userRepo.findByUserId(findRuleByName(ruleName).getOwnerId());
	}

}
