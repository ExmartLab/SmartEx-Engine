package XLab.ExplainableEngine.contextAwareExpGenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import exengine.datamodel.Occurrence;
import exengine.datamodel.Role;
import exengine.datamodel.Rule;
import exengine.datamodel.Error;
import exengine.datamodel.State;
import exengine.datamodel.Technicality;
import exengine.expPresentation.View;
import exengine.contextAwareExpGenerator.ExplanationContextMappingService;
import exengine.datamodel.Context;

@DisplayName("Unit Test Rule Book Engine")
class ExplanationContextMappingServiceTest {

	private ExplanationContextMappingService underTest;

	@BeforeEach
	void setUp() {
		underTest = new ExplanationContextMappingService();
	}

	/*
	 * This method tests the rulebook engine ExplanationContextMappingService.
	 * 
	 * This is an equivalence test for two reasons:
	 * 
	 * 1. The number of different combinations of the ruleBookForErrors and
	 * ruleBookForRules are 3^4 each. This number is infeasible to test, considering
	 * possible errata in the test cases.
	 * 
	 * 2. It is easy to verify that the rules in the ruleBook engine are set up
	 * correctly, while it is difficult to verify that the ruleBook engine is
	 * working correctly.
	 */
	@ParameterizedTest
	@CsvFileSource(resources = "view_test_cases.csv", numLinesToSkip = 1)
	void testGetExplanationView(String causeType, String state, String occurrence, String technicality, String role,
			String expected_view) {

		// Given
		Context context = new Context(Role.valueOf(role), Occurrence.valueOf(occurrence),
				Technicality.valueOf(technicality), State.valueOf(state), null, null);

		Object cause;
		if (causeType.equals("rule")) {
			cause = new Rule(null, null, null, null, null, null, null);
		} else {
			cause = new Error(null, null, null, null, null);
		}

		// When
		View viewType = underTest.getExplanationView(context, cause);

		// Then
		Assertions.assertEquals(View.valueOf(expected_view), viewType);
	}

}
