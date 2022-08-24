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

	public Rule findRuleByRuleId(String ruleId) {
		return ruleRepo.findByRuleId(ruleId);
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
		return userRepo.findByName(userName);
	}

	public User findUserById(String id) {
		User user;
		try {
			user = userRepo.findById(id).get();
		} catch (Exception e) {
			user = null;
		}
		return user;
	}

	public User findUserByUserId(String userId) {
		User user;
		try {
			user = userRepo.findByUserId(userId);
		} catch (Exception e) {
			user = null;
		}
		return user;
	}

	/*
	 * OCCURENCE OPERATIONS
	 */
	
	public void deleteAllOccurrencies() {
		occEntrRepo.deleteAll();
	}

	public void saveNewOccurrenceEntry(OccurrenceEntry occurrenceEntry) {
		occEntrRepo.save(occurrenceEntry);
	}

	public ArrayList<OccurrenceEntry> findOccurrenceEntriesByUserIdAndRuleId(String userId, String ruleId) {
		return occEntrRepo.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
	}

	public Occurrence findOccurrence(String userId, String ruleId, int days) {
		System.out.println("getting occurrence for user " + userId + " and rule " + ruleId);
		ArrayList<OccurrenceEntry> entries = occEntrRepo.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
		int count = 0;
		long reference = (long) (days) * 24l * 60l * 60l * 1000l;
		for (OccurrenceEntry entry : entries)
		{
			System.out.println("looking at entry: " + new Date(entry.getTime()).toString());
			if (entry.getTime() > reference) {
				count++;
				System.out.println("count: " + count);
			}
				
		}
		switch (count) {
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
		String ownerId = findRuleByName(ruleName).getOwnerId();
		return findUserByUserId(ownerId);
	}

}
