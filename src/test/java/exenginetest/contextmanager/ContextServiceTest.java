package exenginetest.contextmanager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import exengine.datamodel.Context;
import exengine.datamodel.Occurrence;
import exengine.datamodel.Role;
import exengine.datamodel.Rule;
import exengine.datamodel.User;
import exengine.datamodel.Error;
import exengine.datamodel.State;
import exengine.datamodel.OccurrenceEntry;
import exengine.datamodel.Technicality;
import exengine.contextmanager.ContextService;
import exengine.database.DatabaseService;

@DisplayName("Unit Test ContextService")
@ExtendWith(MockitoExtension.class)
class ContextServiceTest {

	private Role role;
	private Occurrence occurrence;
	private Technicality technicality;
	private State state;
	private String name;

	@InjectMocks
	private ContextService underTest;

	@Mock
	private DatabaseService dataSer;

	@BeforeEach
	void init() {
		role = Role.COWORKER;
		occurrence = Occurrence.FIRST;
		technicality = Technicality.NONTECH;
		state = State.MEETING;
		name = "Sarah";
	}

	/*
	 * This tests two cases:
	 * 
	 * 1. The explainee is the same person as the rule owner 2. The explaine is not
	 * the same person as the rule owner
	 */
	@DisplayName("Test Context For Cause Case")
	@ParameterizedTest
	@CsvSource({ "true", "false" })
	void testGetAllContextCause(boolean ruleOwnerIsExplainee) {

		// Given
		String ruleId = "rule1";
		String userId = "user1";
		Rule rule = new Rule(null, ruleId, null, null, null, null, null, 0);
		User user = new User(name, 30, role, technicality, state, "London");
		user.setId(userId);
		User ruleOwner;
		if (ruleOwnerIsExplainee) {
			ruleOwner = user;
		} else {
			ruleOwner = new User("Dani", 40, role, technicality, state, "Paris");
		}

		// When
		Mockito.when(dataSer.findOccurrenceEntriesByUserIdAndRuleId(null, ruleId)).thenReturn(new ArrayList<>());
		Mockito.when(dataSer.findOwnerByRuleName(null)).thenReturn(ruleOwner);
		Context context = underTest.getAllContext(rule, user);

		// Then
		if (ruleOwnerIsExplainee) {
			Assertions.assertEquals(Role.OWNER, context.getExplaineeRole());
		} else {
			Assertions.assertEquals(role, context.getExplaineeRole());
		}
		Assertions.assertEquals(occurrence, context.getOccurrence());
		Assertions.assertEquals(technicality, context.getExplaineeTechnicality());
		Assertions.assertEquals(state, context.getExplaineeState());
		Assertions.assertEquals(name, context.getExplaineeName());
		Assertions.assertEquals(ruleOwner.getName(), context.getOwnerName());
	}

	@DisplayName("Test Context For Error Case")
	@Test
	void testGetAllContextError() {

		// Given
		String errorId = "error1";
		Error error = new Error(null, errorId, null, null, null);
		User user = new User(name, 30, role, technicality, state, "London");
		user.setId("id");

		// When
		Mockito.when(dataSer.findOccurrenceEntriesByUserIdAndRuleId(null, errorId)).thenReturn(new ArrayList<>());
		Context context = underTest.getAllContext(error, user);

		// Then
		Assertions.assertEquals(role, context.getExplaineeRole());
		Assertions.assertEquals(occurrence, context.getOccurrence());
		Assertions.assertEquals(technicality, context.getExplaineeTechnicality());
		Assertions.assertEquals(state, context.getExplaineeState());
		Assertions.assertEquals(name, context.getExplaineeName());
		Assertions.assertNull(context.getOwnerName());
	}

	@DisplayName("Test Counting Occurrences")
	@ParameterizedTest
	@CsvSource({ "0, FIRST", "1, SECOND", "2, MORE", "3, MORE" })
	void testCountOccurrence(int numberOfOccurrences, String expectedOutput) {

		// Given
		String explaineeId = "explainee1";
		String ruleId = "rule1";

		ArrayList<OccurrenceEntry> entryList = new ArrayList<>();
		for (int i = 0; i < numberOfOccurrences; i++) {
			LocalDateTime localDateTime = LocalDateTime.now().minusDays(numberOfOccurrences);
			Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
			long miliSeconds = instant.toEpochMilli();
			OccurrenceEntry newEntry = new OccurrenceEntry(explaineeId, ruleId, miliSeconds);
			entryList.add(newEntry);
		}

		// Noice
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(91); // exactly one day before cutoff date
		Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
		long miliSeconds = instant.toEpochMilli();
		OccurrenceEntry newEntry = new OccurrenceEntry(explaineeId, ruleId, miliSeconds);
		entryList.add(newEntry);

		// When
		Mockito.when(dataSer.findOccurrenceEntriesByUserIdAndRuleId(explaineeId, ruleId)).thenReturn(entryList);
		Occurrence result = underTest.findOccurrence(explaineeId, ruleId, 90); // cutoff date 90 days into past

		// Then
		Assertions.assertEquals(expectedOutput, result.toString());

	}
}