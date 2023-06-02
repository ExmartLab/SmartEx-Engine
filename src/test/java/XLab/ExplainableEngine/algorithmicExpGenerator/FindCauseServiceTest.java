package XLab.ExplainableEngine.algorithmicExpGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import exengine.datamodel.Rule;
import exengine.datamodel.Cause;
import exengine.datamodel.Error;
import exengine.algorithmicExpGenerator.FindCauseService;
import exengine.datamodel.LogEntry;

class FindCauseServiceTest {

	TestingObjects testingObjects;

	FindCauseService underTest;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		testingObjects = new TestingObjects();
		underTest = new FindCauseService();
	}

	@DisplayName("Test Finding Candidate Rules Where Four Candidate Rules Exist (Case 1)")
	@Test
	void testFindCandidateRuleCase1() throws IOException, URISyntaxException {

		// Given
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId("scene.tv_playing");
		explanandum.setState(null);

		// When
		ArrayList<Rule> candidateRules = underTest.findCandidateRules(explanandum, dbRules);

		// Then
		Assertions.assertEquals(4, candidateRules.size());
		Assertions.assertEquals("rule 2 (tv mute)", candidateRules.get(0).getRuleName());
		Assertions.assertEquals("rule 3 (constructed rule)", candidateRules.get(1).getRuleName());
	}

	@DisplayName("Test Finding Candidate Rules Where One Candidate Rules Exists (Case 2)")
	@Test
	void testFindCandidateRuleCase2() throws IOException, URISyntaxException {

		// Given
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId("switch.smart_plug_social_room_coffee");
		explanandum.setState("off");

		// When
		ArrayList<Rule> candidateRules = underTest.findCandidateRules(explanandum, dbRules);

		// Then
		Assertions.assertEquals(1, candidateRules.size());
		Assertions.assertEquals("rule 1 (coffee)", candidateRules.get(0).getRuleName());
	}

	@DisplayName("Test Finding Candidate Rules Where Candidate Rules Do Not Exist")
	@Test
	void testFailureFindCandidateRule() throws IOException, URISyntaxException {

		// Given
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId("scene.radio"); // r
		explanandum.setState("off");

		// When
		ArrayList<Rule> candidateRules = underTest.findCandidateRules(explanandum, dbRules);

		// Then
		Assertions.assertEquals(0, candidateRules.size());
	}

	@DisplayName("Test Filtering LogEntries By Time Interval")
	@Test
	void testFilterLogEntriesByTime() {

		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry logEntryFromMiddle = logEntries.get(4);
		LocalDateTime middleTime = logEntryFromMiddle.getLocalDateTime();
		LocalDateTime startTime = middleTime.minusMinutes(5);
		LocalDateTime endTime = middleTime.plusMinutes(5);

		// When
		ArrayList<LogEntry> logEntriesFiltered = underTest.filterLogEntriesByTime(logEntries, startTime, endTime);

		// Then
		Assertions.assertEquals(3, logEntriesFiltered.size());

	}
	
	@DisplayName("Test If Actions Apply (Should)")
	@ParameterizedTest
	@CsvSource({"9, 1", "5, 0", "9, 2", "10, 2"})
	void testActionsApply(int explanandumIndex, int ruleIndex) {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(explanandumIndex);
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		Rule rule = dbRules.get(ruleIndex);
		int tolerance = 1000;
		
		// When
		boolean actionsApply = underTest.actionsApply(explanandum, rule, logEntries, tolerance);
		
		// Then
		Assertions.assertTrue(actionsApply);
	}
	
	@DisplayName("Test If Actions Apply (Should Not)")
	@ParameterizedTest
	@CsvSource({"9, 0", "5, 1", "9, 3", "10, 3"})
	void testActionsApplyNot(int explanandumIndex, int ruleIndex) {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(explanandumIndex);
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		Rule rule = dbRules.get(ruleIndex);
		int tolerance = 1000;
		
		// When
		boolean actionsApply = underTest.actionsApply(explanandum, rule, logEntries, tolerance);
		
		// Then
		Assertions.assertFalse(actionsApply);
	}
	
	@DisplayName("Test If Preconditions Apply (Should)")
	@ParameterizedTest
	@CsvSource({"9, 1", "5, 0"})
	void testPreconditionsApply(int explanandumIndex, int ruleIndex) {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(explanandumIndex);
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		Rule rule = dbRules.get(ruleIndex);
		
		Collections.sort(logEntries, Collections.reverseOrder());
		
		// When
		boolean preconditionsApply = underTest.preconditionsApply(explanandum, rule, logEntries);
		
		// The
		Assertions.assertTrue(preconditionsApply);
		
	}
	
	@DisplayName("Test If Preconditions Apply (Should Not)")
	@ParameterizedTest
	@CsvSource({"5, 1", "9, 4"})
	void testPreconditionsApplyNot(int explanandumIndex, int ruleIndex) {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(explanandumIndex);
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		Rule rule = dbRules.get(ruleIndex);
		
		Collections.sort(logEntries, Collections.reverseOrder());
		
		// When
		boolean preconditionsApply = underTest.preconditionsApply(explanandum, rule, logEntries);
		
		// The
		Assertions.assertFalse(preconditionsApply);
		
	}
	
	@DisplayName("Test If Is Error (Should)")
	@Test
	void testIsError() {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(2);
		ArrayList<Error> dbErrors = testingObjects.getDBErrors();
		
		// When
		Error isError = underTest.getError(explanandum, dbErrors);
		
		// Then
		Assertions.assertNotNull(isError);
		Assertions.assertEquals("No. 104: DEEBOT gets stuck while working and stops", isError.getErrorName());
	}
	
	@DisplayName("Test If Is Error (Should Not)")
	@ParameterizedTest
	@CsvSource({"0", "1", "3", "4", "5", "6", "7", "8", "9", "10"})
	void testIsErrorNot(int explanandumIndex) {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
		LogEntry explanandum = logEntries.get(explanandumIndex);
		ArrayList<Error> dbErrors = testingObjects.getDBErrors();
		
		// When
		Error isError = underTest.getError(explanandum, dbErrors);
		
		// Then
		Assertions.assertNull(isError);
	}
	
	
	@DisplayName("Test findRuleCause For Rule Case")
	@ParameterizedTest
	@CsvSource({ "switch.smart_plug_social_room_coffee, off, 2022-06-23T09:50:50.848452+00:00, [Smart Plug Social Room Coffee|off;]",
			"scene.tv_playing, null, 2022-06-23T11:19:31.037231+00:00, [tv_mute|null;]"})
	void testFindRuleCause(String entityId, String state, String time, String expectedOutput) {

		// Given
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		ArrayList<Error> dbErrors = testingObjects.getDBErrors();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId(entityId);
		explanandum.setState(state);
		explanandum.setTime(time);
		
		// When
		Cause cause = underTest.findCause(explanandum, demoEntries, dbRules, dbErrors);

		// Then
		Assertions.assertEquals(expectedOutput, cause.getActionsString());
	}
	
	@DisplayName("Test findRuleCause For Error Case")
	@ParameterizedTest
	@CsvSource({ "sensor.deebot_last_error, 104, 2022-06-23T09:07:26.933444+00:00, [Deebot last error|104;]"})
	void testFindErrorCause(String entityId, String state, String time, String expectedOutput) {
		// Given
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		ArrayList<Error> dbErrors = testingObjects.getDBErrors();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId(entityId);
		explanandum.setState(state);
		explanandum.setTime(time);

		// When
		Cause cause = underTest.findCause(explanandum, demoEntries, dbRules, dbErrors);

		// Then
		Assertions.assertEquals("[Deebot last error|104;]", cause.getActionsString());
	}
	
	@DisplayName("Test findRuleCause For Null Case")
	@ParameterizedTest
	@CsvSource({ "scene.tv_brightness, null, 2022-06-23T11:19:31.126754+00:00"})
	void testFindErrorCause(String entityId, String state, String time) {
		// Given
		ArrayList<LogEntry> demoEntries = testingObjects.getDemoEntries();
		ArrayList<Rule> dbRules = testingObjects.getDBRules();
		ArrayList<Error> dbErrors = testingObjects.getDBErrors();
		LogEntry explanandum = new LogEntry();
		explanandum.setEntityId(entityId);
		explanandum.setState(state);
		explanandum.setTime(time);

		// When
		Cause cause = underTest.findCause(explanandum, demoEntries, dbRules, dbErrors);

		// Then
		Assertions.assertNull(cause);
	}
}
