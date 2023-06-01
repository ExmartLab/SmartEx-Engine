package exengine.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.*;
import exengine.datamodel.Error;

@Service
public class DatabaseService {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

	@Autowired
	UserRepository userRepo;

	@Autowired
	RuleRepository ruleRepo;

	@Autowired
	ErrorRepository errorRepo;

	@Autowired
	OccurrenceEntryRepository occEntrRepo;

	@Autowired
	EntityRepository entityRepo;

	public void resetDatabase() {
		deleteAllRules();
		deleteAllErrors();
		deleteAllUsers();
		deleteAllEntities();
		if (ExplainableEngineApplication.isDemo()) {
			deleteAllOccurrencies();
			logger.info("Database was completely reset");
		} else {
			logger.info("Database was reset, except for the Occurrencies table");
		}
	}

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
	 * ERROR OPERATIONS
	 */
	public List<exengine.datamodel.Error> findAllErrors() {
		return errorRepo.findAll();
	}

	public void saveNewError(exengine.datamodel.Error error) {
		errorRepo.save(error);
	}

	public void deleteAllErrors() {
		errorRepo.deleteAll();
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
	 * HA ENTITY OPERATIONS
	 */
	public void deleteAllEntities() {
		entityRepo.deleteAll();
	}

	public void saveNewEntity(Entity entity) {
		entityRepo.save(entity);
	}

	public Entity findEntityByEntityId(String entityId) {
		return entityRepo.findByEntityId(entityId);
	}

	public Entity findEntityByDeviceName(String deviceName) {
		return entityRepo.findByDeviceName(deviceName);
	}

	public ArrayList<Entity> findEntitiesByDeviceName(String deviceName) {
		return entityRepo.findEntitiesByDeviceName(deviceName);
	}

	public ArrayList<String> findEntityIdsByDeviceName(String deviceName) {
		ArrayList<Entity> entities = entityRepo.findEntitiesByDeviceName(deviceName);
		ArrayList<String> entityIds = new ArrayList<>();
		for (Entity entity : entities) {
			entityIds.add(entity.getEntityId());
		}
		return entityIds;
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
		long reference = new Date().getTime() - ((days) * 24l * 60l * 60l * 1000l);
		for (OccurrenceEntry entry : entries) {
			String lookAtDate = new Date(entry.getTime()).toString();
			logger.trace("looking at entry: {}", lookAtDate);
			if (entry.getTime() > reference) {
				count++;
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

	/**
	 * Returns a list of unique actions combining the actions of all rules and
	 * errors stores in the database.
	 * 
	 * @Note Unique in the sence, that entityId and state are unique
	 * @See compareTo method in LogEntry
	 * 
	 * @return A list of unique actions
	 */
	public ArrayList<LogEntry> getAllActions() {
		ArrayList<LogEntry> actions = new ArrayList<>();

		List<Rule> rules = ruleRepo.findAll();

		for (Rule rule : rules) {
			for (LogEntry action : rule.getActions()) {
				if (!actions.contains(action)) {
					actions.add(action);
				}
			}
		}

		List<Error> errors = errorRepo.findAll();

		for (Error error : errors) {
			for (LogEntry action : error.getActions()) {
				if (!actions.contains(action)) {
					actions.add(action);
				}
			}
		}

		return actions;
	}

	/*
	 * MISC.
	 */
	public User findOwnerByRuleName(String ruleName) {
		String ownerId = findRuleByName(ruleName).getOwnerId();
		return findUserByUserId(ownerId);
	}

}
