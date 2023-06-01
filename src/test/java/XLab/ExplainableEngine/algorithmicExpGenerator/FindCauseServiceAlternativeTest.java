package XLab.ExplainableEngine.algorithmicExpGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
		Assertions.assertEquals(2, candidateRules.size());
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

	@Test
	void testFilterLogEntriesByTime() {
		
		// Given
		ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
	}
}
