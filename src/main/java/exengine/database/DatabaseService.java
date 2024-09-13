package exengine.database;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.Entity;
import exengine.datamodel.Error;
import exengine.datamodel.FrequencyEntry;
import exengine.datamodel.LogEntry;
import exengine.datamodel.OccurrenceEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.User;

/**
 * The DatabaseService class provides methods for interacting with the database
 * to perform operations related to rules, errors, users, entities, and
 * occurrences.
 */
@Service
public class DatabaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

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

	@Autowired
	FrequencyRepository frequencyRepo;

	/**
	 * Resets the database by deleting all rules, errors, users, and entities. If
	 * the application is in demo mode, it also deletes all occurrences.
	 */
	public void resetDatabase() {
		deleteAllRules();
		deleteAllErrors();
		deleteAllUsers();
		deleteAllEntities();
		if (ExplainableEngineApplication.isDemo()) {
			deleteAllOccurrences();
			LOGGER.info("Database was completely reset");
		} else {
			LOGGER.info("Database was reset, except for the Occurrencies table");
		}
	}

	// RULE OPERATIONS

	/**
	 * Retrieves a list of all rules stored in the database.
	 *
	 * @return the list of all rules
	 */
	public List<Rule> findAllRules() {
		return ruleRepo.findAll();
	}

	/**
	 * Saves a new rule to the database.
	 *
	 * @param rule the rule to be saved
	 */
	public void saveNewRule(Rule rule) {
		ruleRepo.save(rule);
	}

	/**
	 * Deletes all rules from the database.
	 */
	public void deleteAllRules() {
		ruleRepo.deleteAll();
	}

	/**
	 * Finds a rule in the database by its name.
	 *
	 * @param ruleName the name of the rule
	 * @return the rule object if found, or null otherwise
	 */
	public Rule findRuleByName(String ruleName) {
		return ruleRepo.findByName(ruleName);
	}

	// ERROR OPERATIONS

	/**
	 * Retrieves a list of all errors stored in the database.
	 *
	 * @return the list of all errors
	 */
	public List<exengine.datamodel.Error> findAllErrors() {
		return errorRepo.findAll();
	}

	/**
	 * Saves a new error to the database.
	 *
	 * @param error the error to be saved
	 */
	public void saveNewError(exengine.datamodel.Error error) {
		errorRepo.save(error);
	}

	/**
	 * Deletes all errors from the database.
	 */
	public void deleteAllErrors() {
		errorRepo.deleteAll();
	}

	// USER OPERATIONS

	/**
	 * Saves a new user to the database.
	 *
	 * @param user the user to be saved
	 */
	public void saveNewUser(User user) {
		userRepo.save(user);
	}

	/**
	 * Deletes all users from the database.
	 */
	public void deleteAllUsers() {
		userRepo.deleteAll();
	}

	/**
	 * Finds a user in the database by their user ID.
	 *
	 * @param userId the user ID
	 * @return the user object if found, or null otherwise
	 */
	public User findUserByUserId(String userId) {
		User user;
		try {
			user = userRepo.findByUserId(userId);
		} catch (Exception e) {
			user = null;
		}
		return user;
	}

	/**
	 * Finds the owner of a rule by its name.
	 *
	 * @param ruleName the name of the rule
	 * @return the owner user object if found, or null otherwise
	 */
	public User findOwnerByRuleName(String ruleName) {
		String ownerId = findRuleByName(ruleName).getOwnerId();
		return findUserByUserId(ownerId);
	}

	// HA ENTITY OPERATIONS

	/**
	 * Deletes all entities from the database.
	 */
	public void deleteAllEntities() {
		entityRepo.deleteAll();
	}

	/**
	 * Saves a new entity to the database.
	 *
	 * @param entity the entity to be saved
	 */
	public void saveNewEntity(Entity entity) {
		entityRepo.save(entity);
	}

	/**
	 * Finds a list of entity IDs in the database by their device name.
	 *
	 * @param deviceName the device name
	 * @return the list of entity IDs with matching device names
	 */
	public ArrayList<String> findEntityIdsByDeviceName(String deviceName) {
		ArrayList<Entity> entities = entityRepo.findEntitiesByDeviceName(deviceName);
		ArrayList<String> entityIds = new ArrayList<>();
		for (Entity entity : entities) {
			entityIds.add(entity.getEntityId());
		}
		return entityIds;
	}
	


	/**
	 * Finds entity in the database by their entity ID.
	 *
	 * @param entityId
	 * @return entity
	 */
	public Entity findEntityByEntityID(String entityId) {
		Entity entity = entityRepo.findEntityByEntityId(entityId);
		return entity;
	}

	// OCCURENCE OPERATIONS

	/**
	 * Deletes all occurrences from the database.
	 */
	public void deleteAllOccurrences() {
		occEntrRepo.deleteAll();
	}

	/**
	 * Saves a new occurrence entry to the database.
	 *
	 * @param occurrenceEntry the occurrence entry to be saved
	 */
	public void saveNewOccurrenceEntry(OccurrenceEntry occurrenceEntry) {
		occEntrRepo.save(occurrenceEntry);
	}

	/**
	 * Finds occurrence entries in the database by user ID and rule ID.
	 *
	 * @param userId the user ID
	 * @param ruleId the rule ID
	 * @return the list of occurrence entries with matching user ID and rule ID
	 */
	public ArrayList<OccurrenceEntry> findOccurrenceEntriesByUserIdAndRuleId(String userId, String ruleId) {
		return occEntrRepo.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
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

	// FREQUENCY OPERATIONS

	/**
	 * Returns all FrequencyEntry stored in the database.
	 *
	 * @return the list FrequencyEntry
	 */
	public List<FrequencyEntry> getAllFrequencyEntries() {
		return frequencyRepo.findAll();
	}

	/**
	 * Saves a new frequency entry to the database.
	 *
	 * @param frequencyEntry the FrequencyEntry entry to be saved
	 */
	public void saveFrequencyEntry(FrequencyEntry frequencyEntry) {
		frequencyRepo.save(frequencyEntry);
	}

	/**
	 * Finds frequency entries in the database by rule ID
	 *
	 * @param ruleId the rule ID
	 * @return the list of occurrence entries with matching user ID and rule ID
	 */
	public List<FrequencyEntry> findAllFrequencyEntriesByRuleId(String ruleId) {
		return frequencyRepo.findFrequencyEntriesById(ruleId);
	}


}
