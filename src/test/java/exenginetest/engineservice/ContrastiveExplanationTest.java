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
import exengine.engineservice.ContrastiveExplanationService;
import exengine.loader.DatabaseSeeder;

@DisplayName("Integration Test for Building a Contrastive Explanation")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = ExplainableEngineApplication.class)
public class ContrastiveExplanationTest {

    @Autowired
    private DatabaseSeeder seeder;

    @Autowired
    private ContrastiveExplanationService underTest;

    @BeforeAll
    void init() {
        seeder.seedDatabaseForTesting();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/contrastive_explanation_test_cases.csv", numLinesToSkip = 1, delimiter = ';')
    void integrationTestGetExplanation(String userId, String device, String expectedExplanation) {
        // When
        String explanation = underTest.getExplanation(30, userId, device);

        // Then
        Assertions.assertEquals(expectedExplanation, explanation);
    }

}
