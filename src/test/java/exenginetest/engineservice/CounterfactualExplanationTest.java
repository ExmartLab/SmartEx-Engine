package exenginetest.engineservice;

import exengine.ExplainableEngineApplication;
import exengine.engineservice.CausalExplanationService;
import exengine.engineservice.CounterfactualExplanationService;
import exengine.loader.DatabaseSeeder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@DisplayName("Integration Test for Building a Counterfactual Explanation")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = ExplainableEngineApplication.class)
public class CounterfactualExplanationTest {

    @Autowired
    private DatabaseSeeder seeder;

    @Autowired
    private CounterfactualExplanationService underTest;

    @BeforeEach
    void setUp() {
        seeder.seedDatabaseForTesting();
    }

    @Disabled
    @ParameterizedTest
    @CsvFileSource(resources = "causal_explanation_test_cases.csv", numLinesToSkip = 1, delimiter = ';')
    void integrationTestGetExplanation(String userId, String device, String expectedExplanation) {
        // When
        String explanation = underTest.getExplanation(30, userId, device);

        // Then
        Assertions.assertEquals(expectedExplanation, explanation);
    }


}
