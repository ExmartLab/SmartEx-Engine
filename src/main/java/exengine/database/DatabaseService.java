package exengine.database;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.datamodel.*;

@Service
public class DatabaseService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	RuleRepository ruleRepo;

	@Autowired
	OccurrenceEntryRepository occEntrRepo;

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

	public String findRuleDescriptionByRuleName(String ruleName) {
		return ruleRepo.findByName(ruleName).getRuleDescription();
	}

	/*
	 * USER OPERATIONS
	 */
	public void saveNewUser(User user) {
		userRepo.save(user);
	}

	public void deleteAllUsers() {
		userRepo.deleteAll();
	}

	public User findUserByName(String userName) {
		System.out.println("finding user by name: " + userName);
		User foundUser = userRepo.findByName(userName);
		System.out.println(foundUser.toString());

		return foundUser;

//		return userRepo.findByName(userName);
	}

	public User findUserById(String userId) {
		User user;
		try {
			user = userRepo.findById(userId).get();
		} catch (Exception e) {
			user = null;
		}
		return user;
	}

	public User findUserByUserId(int userId) {
		return userRepo.findByUserId(userId);
	}

	
	/*
	 * OCCURENCE OPERATIONS
	 */
	public ArrayList<OccurrenceEntry> findOccurrenceEntriesByUserIdAndRuleId(int userId, int ruleId) {
		return occEntrRepo.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
	}

	public Occurrence findOccurrence(int userId, int ruleId, int days){
		ArrayList<OccurrenceEntry> entries = occEntrRepo.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
		int count = 0;
		Timestamp reference = new Timestamp(new Date().getTime() - (long)(days)*24l*60l*60l*1000l);
		for(OccurrenceEntry entry : entries)
			if(entry.getTime().after(reference))
				count++;
		
		switch(count) {
		case 0:
			return Occurrence.FIRST;
		case 1:
			return Occurrence.SECOND;
		default:
			return Occurrence.MORE;
		}
	}

	/*
	 * MISC.
	 */
	public User findOwnerByRuleName(String ruleName) {
		int ownerId = findRuleByName(ruleName).getOwnerId();
		return findUserByUserId(ownerId);
	}

}
