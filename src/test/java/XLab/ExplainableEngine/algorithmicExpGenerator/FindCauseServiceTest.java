package XLab.ExplainableEngine.algorithmicExpGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import exengine.algorithmicExpGenerator.FindCauseService;
import exengine.datamodel.Cause;
import exengine.datamodel.Error;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;

class FindCauseServiceTest {

	private FindCauseService findCauseSer;

	private TestingObjects testObjects;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		findCauseSer = new FindCauseService();
		testObjects = new TestingObjects();
	}

	@ParameterizedTest
	@CsvSource({ "switch.smart_plug_social_room_coffee, [Smart Plug Social Room Coffee|off;]",
			"scene.tv_playing, [tv_mute|null;]"})
	void testFindRuleCause(String entityId, String expectedOutput) {

		// Given
		ArrayList<LogEntry> demoEntries = testObjects.getDemoEntries();
		ArrayList<Rule> dbRules = testObjects.getDBRules();
		ArrayList<Error> dbErrors = testObjects.getDBErrors();
		ArrayList<String> entityIds = new ArrayList<String>();
		entityIds.add(entityId);

		// When
		Cause cause = findCauseSer.findCause(demoEntries, dbRules, dbErrors, entityIds);

		// Then
		Assertions.assertEquals(expectedOutput, cause.getActionsString());
	}
	
	@Test
	void testFindErrorCause() {
		// Given
		ArrayList<LogEntry> demoEntries = testObjects.getDemoEntries();
		ArrayList<Rule> dbRules = testObjects.getDBRules();
		ArrayList<Error> dbErrors = testObjects.getDBErrors();
		ArrayList<String> entityIds = new ArrayList<String>();
		entityIds.add("sensor.deebot_last_error");
		entityIds.add("vacuum.deebot");

		// When
		Cause cause = findCauseSer.findCause(demoEntries, dbRules, dbErrors, entityIds);

		// Then
		Assertions.assertEquals("[Deebot last error|104;]", cause.getActionsString());
	}

}
