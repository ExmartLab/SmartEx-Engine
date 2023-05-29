package XLab.ExplainableEngine.loader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import exengine.ExplainableEngineApplication;
import exengine.database.DatabaseService;
import exengine.datamodel.Role;
import exengine.datamodel.Rule;
import exengine.datamodel.Error;
import exengine.datamodel.Technicality;
import exengine.datamodel.User;
import exengine.datamodel.LogEntry;
import exengine.loader.DatabaseSeeder;

@SpringBootTest(classes = ExplainableEngineApplication.class)
class DatabaseSeederTest {
	
	@Autowired
	private DatabaseSeeder underTest;

	@Autowired
	private DatabaseService dataSer;
	
	@Test
	void testSeedUsers() {
		// Given
		String name = "Alice";
		String userId = "1";
		Role role = Role.COWORKER;
		Technicality technicality = Technicality.MEDTECH;
		
		// When
		underTest.seedDatabaseForTesting();
		
		// Then
		
		// Test attributes of first user in detail
		User user = dataSer.findUserByUserId("1");
		Assertions.assertEquals(name, user.getName());
		Assertions.assertEquals(userId, user.getUserId());
		Assertions.assertEquals(role, user.getRole());
		Assertions.assertEquals(technicality, user.getTechnicality());
		
		// Test remaining users only in their existence
		for (int i = 2; i <= 8; i++) {
			user = dataSer.findUserByUserId(""+i);
			
			Assertions.assertNotNull(user);
			Assertions.assertNotNull(user.getName());
			Assertions.assertNotNull(user.getStateString());
			Assertions.assertNotNull(user.getUserId());
			Assertions.assertNotNull(user.getRole());
			Assertions.assertNotNull(user.getTechnicality());
		}
		
	}
	
	@Test
	void testSeedRules() {
		// Given
		String name = "rule 1 (coffee)";
		String ruleId = "1";
		
		LogEntry ruleEntry = new LogEntry();
		ruleEntry.setName("Rule: Block High Coffee Consumption (sc1)");
		ruleEntry.setEntityId("automation.test_scenario_watching_tv_light_off");
		ruleEntry.setState(null);
		
		ArrayList<LogEntry> triggers = new ArrayList<LogEntry>();
		LogEntry triggerEntry = new LogEntry();
		triggerEntry.setName("state change");
		triggerEntry.setEntityId("scene.state_change");
		triggerEntry.setState(null);
		triggers.add(triggerEntry);
		
		ArrayList<String> conditions = new ArrayList<String>();
		conditions.add("daily energy consumption is higher than the threshold");
		
		ArrayList<LogEntry> actions = new ArrayList<LogEntry>();
		LogEntry actionEntry = new LogEntry();
		actionEntry.setName("Smart Plug Social Room Coffee");
		actionEntry.setEntityId("switch.smart_plug_social_room_coffee");
		actionEntry.setState("off");
		actions.add(actionEntry);
		
		String ownerId = "1";
		String ruleDescription = "Rule_1: allows coffee to be made only until the daily energy consumption threshold is reached";
		
		// When
		underTest.seedDatabaseForTesting();
		
		// Then
		List<Rule> ruleList = dataSer.findAllRules();
		Assertions.assertNotNull(ruleList.get(0));
		Assertions.assertNotNull(ruleList.get(1));
		
		Rule rule = ruleList.get(0);
		
		Assertions.assertEquals(name, rule.getRuleName());
		Assertions.assertEquals(ruleId, rule.getRuleId());
		Assertions.assertEquals(ruleEntry, rule.getRuleEntry());
		Assertions.assertEquals(triggers, rule.getTrigger());
		Assertions.assertEquals(conditions, rule.getConditions());
		Assertions.assertEquals(actions, rule.getActions());
		Assertions.assertEquals(ownerId, rule.getOwnerId());
		Assertions.assertEquals(ruleDescription, rule.getRuleDescription());
	}
	
	@Test
	void testSeedErrors() {
		// Given
		String name = "No. 04: DEEBOT gets stuck while working and stops";
		String errorId = "e1";
		
		ArrayList<LogEntry> actions = new ArrayList<LogEntry>();
		LogEntry actionEntry = new LogEntry();
		actionEntry.setName("Deebot last error");
		actionEntry.setEntityId("sensor.deebot_last_error");
		actionEntry.setState("104");
		actions.add(actionEntry);
		
		String implication = "DEEBOT is tangled with something on the floor or might be stuck under furniture";
		String solution = "If DEEBOT can not free itself, manually remove the obstacles and restart";
		
		// When
		underTest.seedDatabaseForTesting();
		
		// Then
		List<Error> errorList = dataSer.findAllErrors();
		Assertions.assertNotNull(errorList.get(0));
		
		Error error = errorList.get(0);
		
		Assertions.assertEquals(name, error.getErrorName());
		Assertions.assertEquals(errorId, error.getErrorId());
		Assertions.assertEquals(actions, error.getActions());
		Assertions.assertEquals(implication, error.getImplication());
		Assertions.assertEquals(solution, error.getSolution());
	}

}
