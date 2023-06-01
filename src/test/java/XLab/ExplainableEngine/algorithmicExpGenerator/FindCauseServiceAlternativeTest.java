package XLab.ExplainableEngine.algorithmicExpGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import exengine.datamodel.Rule;
import exengine.algorithmicExpGenerator.FindCauseServiceAlternative;
import exengine.datamodel.LogEntry;

class FindCauseServiceAlternativeTest {

	TestingObjects testingObjects;

	FindCauseServiceAlternative underTest;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		testingObjects = new TestingObjects();
		underTest = new FindCauseServiceAlternative();
	}

	@DisplayName("Test Finding Candidate Rules Where Two Candidate Rules Exist (Case 1)")
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
		Assertions.assertEquals(3, candidateRules.size());
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
}
