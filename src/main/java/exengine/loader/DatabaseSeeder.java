package exengine.loader;

import java.io.InputStream;

import javax.annotation.PostConstruct;

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

@Component
public class DatabaseSeeder {

	private final DatabaseService dataSer;
	private final ResourceLoader resourceLoader;

	@Autowired
	public DatabaseSeeder(DatabaseService dataSer, ResourceLoader resourceLoader) {
		this.dataSer = dataSer;
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	public void seedUsers() throws Exception {
		List<Map<String, Object>> dataList = loadDataMap(ExplainableEngineApplication.FILE_NAME_USERS);

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
		System.out.println("Users seeded");
	}

	@PostConstruct
	public void seedEntities() throws Exception {
		List<Map<String, Object>> dataList = loadDataMap(ExplainableEngineApplication.FILE_NAME_ENTITIES);

		for (Map<String, Object> dataMap : dataList) {
			Entity entity = new Entity();
			if (dataMap.containsKey("entityId")) {
				entity.setEntityId(dataMap.get("entityId").toString());
			}
			if (dataMap.containsKey("deviceName")) {
				entity.setDevice(dataMap.get("deviceName").toString());
			}
			dataSer.saveNewEntity(entity);
		}
		System.out.println("Entities seeded");
	}

	@PostConstruct
	public void seedRules() throws Exception {
		List<Map<String, Object>> dataList = loadDataMap(ExplainableEngineApplication.FILE_NAME_RULES);

		for (Map<String, Object> dataMap : dataList) {

			Rule rule = new Rule();

			// name (type: String)
			if (dataMap.containsKey("name")) {
				rule.setRuleName(dataMap.get("name").toString());
			}

			// ruleId (type: String)
			if (dataMap.containsKey("ruleId")) {
				rule.setRuleId(dataMap.get("ruleId").toString());
			}

			// ruleEntry (type: LogEntry)
			if (dataMap.containsKey("ruleEntry")) {
				@SuppressWarnings("unchecked")
				Map<String, Object> ruleEntryMap = (Map<String, Object>) dataMap.get("ruleEntry");
				LogEntry ruleEntry = generateLogEntry(ruleEntryMap);
				rule.setRuleEntry(ruleEntry);

			}

			// triggers (type: ArrayList<LogEntry>)
			if (dataMap.containsKey("triggers")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> triggersMap = (List<Map<String, Object>>) dataMap.get("triggers");
				ArrayList<LogEntry> triggers = new ArrayList<LogEntry>();

				for (Map<String, Object> dataMapLower : triggersMap) {
					LogEntry trigger = generateLogEntry(dataMapLower);
					triggers.add(trigger);
				}

				rule.setTrigger(triggers);

			}
			
			// conditions (type: ArrayList<String>)
			if (dataMap.containsKey("conditions")) {
				@SuppressWarnings("unchecked")
				ArrayList<String> conditions = (ArrayList<String>) dataMap.get("conditions");
				rule.setConditions(conditions);
			}
			
			// actions (type: ArrayList<LogEntry>)
			if (dataMap.containsKey("actions")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> actionsMap = (List<Map<String, Object>>) dataMap.get("actions");
				ArrayList<LogEntry> actions = new ArrayList<LogEntry>();

				for (Map<String, Object> dataMapLower : actionsMap) {
					LogEntry action = generateLogEntry(dataMapLower);
					actions.add(action);
				}

				rule.setActions(actions);

			}
			
			// ownerId (type: String)
			if (dataMap.containsKey("ownerId")) {
				rule.setOwnerId(dataMap.get("ownerId").toString());
			}

			// ruleDescription (type: String)
			if (dataMap.containsKey("ruleDescription")) {
				rule.setRuleDescription(dataMap.get("ruleDescription").toString());
			}
						
			dataSer.saveNewRule(rule);
		}
		System.out.println("Rules seeded");
	}
	
	@PostConstruct
	public void seedErrors() throws Exception {
		List<Map<String, Object>> dataList = loadDataMap(ExplainableEngineApplication.FILE_NAME_ERRORS);

		for (Map<String, Object> dataMap : dataList) {

			Error error = new Error();

			// name (type: String)
			if (dataMap.containsKey("name")) {
				error.setErrorName(dataMap.get("name").toString());
			}

			// errorId (type: String)
			if (dataMap.containsKey("errorId")) {
				error.setErrorId(dataMap.get("errorId").toString());
			}
			
			// actions (type: ArrayList<LogEntry>)
			if (dataMap.containsKey("actions")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> actionsMap = (List<Map<String, Object>>) dataMap.get("actions");
				ArrayList<LogEntry> actions = new ArrayList<LogEntry>();

				for (Map<String, Object> dataMapLower : actionsMap) {
					LogEntry action = generateLogEntry(dataMapLower);
					actions.add(action);
				}

				error.setActions(actions);

			}
			
			// implication (type: String)
			if (dataMap.containsKey("implication")) {
				error.setImplication(dataMap.get("implication").toString());
			}

			// solution (type: String)
			if (dataMap.containsKey("solution")) {
				error.setSolution(dataMap.get("solution").toString());
			}
						
			dataSer.saveNewError(error);
		}
		System.out.println("Errors seeded");
	}
	
	private LogEntry generateLogEntry(Map<String, Object> dataMapLower) {
		LogEntry logEntry = new LogEntry();
		
		if (dataMapLower.containsKey("name")) {
			logEntry.setName(dataMapLower.get("name").toString());
		}

		if (dataMapLower.containsKey("entity_id")) {
			logEntry.setEntity_id(dataMapLower.get("entity_id").toString());
		}

		if (dataMapLower.containsKey("state")) {
			logEntry.setState(dataMapLower.get("state").toString());
		}
		
		return logEntry;
	}

	private List<Map<String, Object>> loadDataMap(String path) throws Exception {
		Resource resource = resourceLoader.getResource("classpath:" + path);
		InputStream inputStream = resource.getInputStream();
		Yaml yaml = new Yaml();
		return yaml.load(inputStream);
	}

}
