package exengine.loader;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import exengine.ExplainableEngineApplication;
import exengine.database.DatabaseService;
import exengine.datamodel.Role;
import exengine.datamodel.Technicality;
import exengine.datamodel.User;
import exengine.datamodel.Rule;
import exengine.datamodel.Error;
import exengine.datamodel.Entity;
import exengine.datamodel.LogEntry;

/**
 * The DatabaseSeeder class is responsible for seeding the database with initial
 * data. It provides methods to seed users, entities, rules, and errors into the
 * database.
 */

@Component
public class DatabaseSeeder {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSeeder.class);

	private final DatabaseService dataSer;
	private final ResourceLoader resourceLoader;

	/**
	 * Constructs a new DatabaseSeeder instance.
	 * 
	 * @param dataSer        The DatabaseService used
	 * @param resourceLoader The ResourceLoader used for loading resources
	 */
	@Autowired
	public DatabaseSeeder(DatabaseService dataSer, ResourceLoader resourceLoader) {
		this.dataSer = dataSer;
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Seeds the database with initial data. This method resets the database and
	 * then seeds it with data from YAML files.
	 * 
	 * @See ExplanainableEngineApplication class
	 */
	@PostConstruct
	public void seedDatabase() {
		dataSer.resetDatabase();
		try {
			seedUsers(ExplainableEngineApplication.FILE_NAME_USERS);
			seedEntities(ExplainableEngineApplication.FILE_NAME_ENTITIES);
			seedRules(ExplainableEngineApplication.FILE_NAME_RULES);
			seedErrors(ExplainableEngineApplication.FILE_NAME_ERRORS);
		} catch (Exception e) {
		    // Handle other exceptions
		    LOGGER.error("An unexpected error occurred while seeding data: " + e.getMessage());
		}
	}

	/**
	 * Seeds the database with testing data. This method resets the database and
	 * then seeds it with testing data from YAML files under the testingData
	 * foulder.
	 * 
	 * @Note it is used for integration testing only.
	 */
	public void seedDatabaseForTesting() {
		dataSer.resetDatabase();
		final String testingPrefix = "testingData/";
		try {
			seedUsers(testingPrefix + ExplainableEngineApplication.FILE_NAME_USERS);
			seedEntities(testingPrefix + ExplainableEngineApplication.FILE_NAME_ENTITIES);
			seedRules(testingPrefix + ExplainableEngineApplication.FILE_NAME_RULES);
			seedErrors(testingPrefix + ExplainableEngineApplication.FILE_NAME_ERRORS);
		} catch (Exception e) {
		    // Handle other exceptions
		    LOGGER.error("An unexpected error occurred while seeding data: " + e.getMessage());
		}

		LOGGER.info("Database reset and reseeded with testing data");
	}

	/**
	 * Seeds the users into the database.
	 * 
	 * @param fileName The name of the YAML file containing user data.
	 */
	private void seedUsers(String fileName) {
		List<Map<String, Object>> dataList = loadDataMap(fileName);

		for (Map<String, Object> dataMap : dataList) {
			User user = new User();
			if (dataMap.containsKey("name")) {
				user.setName(dataMap.get("name").toString());
			}
			if (dataMap.containsKey("userid")) {
				user.setUserId(dataMap.get("userid").toString());
			}
			if (dataMap.containsKey("role")) {
				String roleString = dataMap.get("role").toString();
				user.setRole(Role.valueOf(roleString));
			}
			if (dataMap.containsKey("technicality")) {
				String technicalityString = dataMap.get("technicality").toString();
				user.setTechnicality(Technicality.valueOf(technicalityString));
			}
			dataSer.saveNewUser(user);
		}
		LOGGER.info("Users seeded to database");
	}

	/**
	 * Seeds the entities into the database.
	 * 
	 * @param fileName The name of the YAML file containing entity data.
	 */
	private void seedEntities(String fileName) {
		List<Map<String, Object>> dataList = loadDataMap(fileName);

		for (Map<String, Object> dataMap : dataList) {
			
			String entityId = tryToGet("entityId", dataMap);
			String deviceName = tryToGet("deviceName", dataMap);
			
			dataSer.saveNewEntity(new Entity(entityId, deviceName));
		}
		LOGGER.info("Entities seeded to database");
	}

	/**
	 * Seeds the rules into the database.
	 * 
	 * @param fileName The name of the YAML file containing rule data.
	 */
	@SuppressWarnings("unchecked")
	private void seedRules(String fileName) {
		List<Map<String, Object>> dataList = loadDataMap(fileName);

		for (Map<String, Object> dataMap : dataList) {

			// name (type: String)
			String name = tryToGet("name", dataMap);

			// ruleId (type: String)
			String ruleId = tryToGet("ruleId", dataMap);

			// triggers (type: ArrayList<LogEntry>)
			ArrayList<LogEntry> triggers = new ArrayList<>();
			if (dataMap.containsKey("triggers")) {
				List<Map<String, Object>> triggersMap = (List<Map<String, Object>>) dataMap.get("triggers");
				for (Map<String, Object> dataMapLower : triggersMap) {
					LogEntry trigger = generateLogEntry(dataMapLower);
					triggers.add(trigger);
				}
			}

			// conditions (type: ArrayList<String>)
			ArrayList<String> conditions = new ArrayList<>();
			if (dataMap.containsKey("conditions")) {
				conditions = (ArrayList<String>) dataMap.get("conditions");
			}

			// actions (type: ArrayList<LogEntry>)
			ArrayList<LogEntry> actions = new ArrayList<>();
			if (dataMap.containsKey("actions")) {
				List<Map<String, Object>> actionsMap = (List<Map<String, Object>>) dataMap.get("actions");
				for (Map<String, Object> dataMapLower : actionsMap) {
					LogEntry action = generateLogEntry(dataMapLower);
					actions.add(action);
				}
			}

			// ownerId (type: String)
			String ownerId = tryToGet("ownerId", dataMap);

			// ruleDescription (type: String)
			String ruleDescription = tryToGet("ruleDescription", dataMap);

			dataSer.saveNewRule(new Rule(name, ruleId, triggers, conditions, actions, ownerId, ruleDescription));
		}
		LOGGER.info("Rules seeded to database");
	}

	/**
	 * Seeds the errors into the database.
	 * 
	 * @param fileName The name of the YAML file containing error data.
	 */
	private void seedErrors(String fileName) {
		List<Map<String, Object>> dataList = loadDataMap(fileName);

		for (Map<String, Object> dataMap : dataList) {

			// name (type: String)
			String name = tryToGet("name", dataMap);

			// errorId (type: String)
			String errorId = tryToGet("errorId", dataMap);

			// actions (type: ArrayList<LogEntry>)
			ArrayList<LogEntry> actions = new ArrayList<>();
			if (dataMap.containsKey("actions")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> actionsMap = (List<Map<String, Object>>) dataMap.get("actions");

				for (Map<String, Object> dataMapLower : actionsMap) {
					LogEntry action = generateLogEntry(dataMapLower);
					actions.add(action);
				}
			}

			// implication (type: String)
			String implication = tryToGet("implication", dataMap);

			// solution (type: String)
			String solution = tryToGet("solution", dataMap);

			dataSer.saveNewError(new Error(name, errorId, actions, implication, solution));
		}
		LOGGER.info("Errors seeded to database");
	}

	/**
	 * Seeds the entities into the database. 
	 * 
	 * @Note Only loads name, entityId and state
	 * 
	 * @param fileName The name of the YAML file containing entity data.
	 */
	private LogEntry generateLogEntry(Map<String, Object> dataMapLower) {

		String name = tryToGet("name", dataMapLower);
		String entityId = tryToGet("entity_id", dataMapLower);
		String state = tryToGet("state", dataMapLower);

		return new LogEntry(null, name, state, entityId, null);
	}

	/**
	 * Loads a list of map objects from file.
	 * 
	 * @param path file path
	 * @return list of map objects t
	 */
	private List<Map<String, Object>> loadDataMap(String path) {
		Resource resource = resourceLoader.getResource("classpath:" + path);
		InputStream inputStream = null;
		try {
			inputStream = resource.getInputStream();
		} catch (IOException e) {
		    LOGGER.error("An exception occurred while loading a datamap: " + e.getMessage());
		}
		Yaml yaml = new Yaml();
		return yaml.load(inputStream);
	}

	/**
	 * Scans a map for a key and returns it if available.
	 * 
	 * @param key     the key to look for in the map
	 * @param dataMap the data map to scan
	 * @return the value to the key, if the key exists, else a String containing
	 *         "null"
	 */
	String tryToGet(String key, Map<String, Object> dataMap) {
		if (dataMap.containsKey(key)) {
			return dataMap.get(key).toString();
		} else {
			return "null";
		}
	}

}
