package exenginetest.engineservice;

import java.util.Arrays;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import exengine.database.DatabaseService;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.engineservice.CausalExplanationService;
import exenginetest.algorithmicexplanationgenerator.TestingObjects;
import exengine.datamodel.Error;

@DisplayName("Unit Test CreateExService Preparation Functions")
@ExtendWith(MockitoExtension.class)
class CreateExServiceTest {

	@InjectMocks
	private CausalExplanationService underTest;

	@Mock
	private DatabaseService dataSer;

	@DisplayName("Test populateDemoEntries To Load Properly Formatted JSON")
	@Test
	void testPopulateDemoEntries() throws IOException, URISyntaxException {
		// Given
		String fileName = "testingData/demoLogs.json";

		// When
		ArrayList<LogEntry> logEntries = underTest.loadDemoEntries(fileName);

		// Then
		Assertions.assertEquals(30, logEntries.size());
		for (LogEntry logEntry : logEntries) {
			Assertions.assertNotNull(logEntry);
		}
	}

	@DisplayName("Test Getting Explanandum Where Device Is tv")
	@Test
	void testGetExplanandum() throws IOException, URISyntaxException {

		// Given
		TestingObjects testingObjects = new TestingObjects();
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<LogEntry> actions = initActions();

		// When
		Mockito.when(dataSer.findEntityIdsByDeviceName("tv"))
				.thenReturn(new ArrayList<String>(Arrays.asList("scene.tv_playing")));
		Mockito.when(dataSer.getAllActions()).thenReturn(actions);

		LogEntry explanandum = underTest.getExplanandumsLogEntry("tv", demoEntries);

		// Then
		Assertions.assertNotNull(explanandum.getTime());
		Assertions.assertNotNull(explanandum.getName());
		Assertions.assertEquals("scene.tv_playing", explanandum.getEntityId());
	}

	@DisplayName("Test Getting Explanandum Where Device Is coffee_machine")
	@Test
	void testGetExplanandumCase2() throws IOException, URISyntaxException {

		// Given
		TestingObjects testingObjects = new TestingObjects();
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<LogEntry> actions = initActions();

		// When
		Mockito.when(dataSer.findEntityIdsByDeviceName("coffee_machine"))
				.thenReturn(new ArrayList<String>(Arrays.asList("switch.smart_plug_social_room_coffee")));
		Mockito.when(dataSer.getAllActions()).thenReturn(actions);

		LogEntry explanandum = underTest.getExplanandumsLogEntry("coffee_machine", demoEntries);

		// Then
		Assertions.assertNotNull(explanandum.getTime());
		Assertions.assertNotNull(explanandum.getName());
		Assertions.assertEquals("switch.smart_plug_social_room_coffee", explanandum.getEntityId());
	}

	@DisplayName("Test Getting Explanandum Where Device Is robo_cleaner (Which Has Two EntityId)")
	@Test
	void testGetExplanandumCase3() throws IOException, URISyntaxException {

		// Given
		TestingObjects testingObjects = new TestingObjects();
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<LogEntry> actions = initActions();

		// When
		Mockito.when(dataSer.findEntityIdsByDeviceName("robo_cleaner"))
				.thenReturn(new ArrayList<String>(Arrays.asList("sensor.deebot_last_error", "vacuum.deebot")));
		Mockito.when(dataSer.getAllActions()).thenReturn(actions);

		LogEntry explanandum = underTest.getExplanandumsLogEntry("robo_cleaner", demoEntries);

		// Then
		Assertions.assertNotNull(explanandum.getTime());
		Assertions.assertNotNull(explanandum.getName());
		Assertions.assertEquals("sensor.deebot_last_error", explanandum.getEntityId());
	}

	@DisplayName("Test Getting Explanandum Where Device Is Not Provided")
	@Test
	void testGetExplanandumCase4() throws IOException, URISyntaxException {

		// Given
		TestingObjects testingObjects = new TestingObjects();
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<LogEntry> actions = initActions();

		// When
		Mockito.when(dataSer.getAllActions()).thenReturn(actions);
		LogEntry explanandum = underTest.getExplanandumsLogEntry("unknown", demoEntries);

		// Then
		Assertions.assertNotNull(explanandum.getTime());
		Assertions.assertNotNull(explanandum.getName());
		Assertions.assertEquals("scene.tv_playing", explanandum.getEntityId());

	}

	private ArrayList<LogEntry> initActions() throws IOException, URISyntaxException {
		TestingObjects testingObjects = new TestingObjects();
		List<Rule> rules = testingObjects.getDBRules();
		List<Error> errors = testingObjects.getDBErrors();
		ArrayList<LogEntry> actions = new ArrayList<>();
		actions.add(rules.get(0).getActions().get(0));
		actions.add(rules.get(1).getActions().get(0));
		actions.add(rules.get(2).getActions().get(0));
		actions.add(rules.get(3).getActions().get(0));
		actions.add(rules.get(4).getActions().get(0));
		actions.add(errors.get(0).getActions().get(0));
		return actions;
	}
}
