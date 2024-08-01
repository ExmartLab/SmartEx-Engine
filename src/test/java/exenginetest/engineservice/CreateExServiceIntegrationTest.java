package exenginetest.engineservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import exengine.ExplainableEngineApplication;
import exengine.engineservice.CausalExplanationService;
import exengine.loader.DatabaseSeeder;

@DisplayName("Integration Test for Building an Explanation")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = ExplainableEngineApplication.class)
class CreateExServiceIntegrationTest {

	@Autowired
	private DatabaseSeeder seeder;

	@Autowired
	private CausalExplanationService underTest;

	@BeforeAll
	void init() {
		seeder.seedDatabaseForTesting();
	}

	/**
	 * Test Goal: Verify that the interplay of spoke services with the
	 * CreateExService hub works.
	 * 
	 * 
	 * Comments:
	 * 
	 * Assumption that min parameter of getExplanation irrelevant, because the
	 * application is in demo mode, per default
	 * 
	 * Order of test cases (rows) in CSV is relevant
	 */
	@ParameterizedTest
	@CsvFileSource(resources = "explanation_test_cases.csv", numLinesToSkip = 1, delimiter = ';')
	void integrationTestGetExplanation(String userId, String device, String expectedExplanation) {
		// When
		String explanation = underTest.getExplanation(30, userId, device);

		// Then
		Assertions.assertEquals(expectedExplanation, explanation);
	}

}
