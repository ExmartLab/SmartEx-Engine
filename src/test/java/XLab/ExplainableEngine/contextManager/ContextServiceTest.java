package XLab.ExplainableEngine.contextManager;

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

import java.util.ArrayList;

import exengine.datamodel.Context;
import exengine.datamodel.Occurrence;
import exengine.datamodel.Role;
import exengine.datamodel.Rule;
import exengine.datamodel.User;
import exengine.datamodel.RuleCause;
import exengine.datamodel.State;
import exengine.datamodel.Technicality;
import exengine.contextManager.ContextService;
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
	 * 1. The explainee is the same person as the rule owner
	 * 2. The explaine is not the same person as the rule owner
	 */
	@DisplayName("Test Context For Cause Case")
	@ParameterizedTest
	@CsvSource({"true", "false"})
	void testGetAllContextCause(boolean ruleOwnerIsExplainee) {
		
		// Given
		RuleCause cause = new RuleCause(null, new ArrayList<>(), new ArrayList<>(), new Rule());
		User user = new User(name, 30, role, technicality, state, "London");
		user.setId("id");
		User ruleOwner;
		if (ruleOwnerIsExplainee) {
			ruleOwner = user;
		} else {
			ruleOwner = new User("Dani", 40, role, technicality, state, "Paris");
		}

		// When
		Mockito.when(dataSer.findOccurrence(null, null, 90))
				.thenReturn(occurrence);
		Mockito.when(dataSer.findOwnerByRuleName(null)).thenReturn(ruleOwner);
		Context context = underTest.getAllContext(cause, user);

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
		RuleCause cause = new RuleCause(null, new ArrayList<>(), new ArrayList<>(), new Rule());
		User user = new User(name, 30, role, technicality, state, "London");
		user.setId("id");

		// When
		Mockito.when(dataSer.findOccurrence(null, null, 90))
				.thenReturn(occurrence);
		Context context = underTest.getAllContext(cause, user);

		// Then
		Assertions.assertEquals(role, context.getExplaineeRole());
		Assertions.assertEquals(occurrence, context.getOccurrence());
		Assertions.assertEquals(technicality, context.getExplaineeTechnicality());
		Assertions.assertEquals(state, context.getExplaineeState());
		Assertions.assertEquals(name, context.getExplaineeName());
		Assertions.assertEquals("no owner", context.getOwnerName());
	}
}