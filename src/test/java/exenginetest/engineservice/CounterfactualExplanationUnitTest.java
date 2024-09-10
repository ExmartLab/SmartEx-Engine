package exenginetest.engineservice;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.datamodel.LogEntry;
import exengine.engineservice.CounterfactualExplanationService;
import exenginetest.algorithmicexplanationgenerator.TestingObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

@DisplayName("Unit Test Counterfactual Explanation")
public class CounterfactualExplanationUnitTest {

    TestingObjects testingObjects;

    CounterfactualExplanationService underTest;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        testingObjects = new TestingObjects();
        underTest = new CounterfactualExplanationService();
    }

    @Test
    void testGetPreviousLogEntry() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        LogEntry explanandum = logEntries.get(7);
        LogEntry expectedLogEntry = logEntries.get(6);

        //When
        LogEntry previous = underTest.getPreviousLogEntry(explanandum, logEntries);
        System.out.println("previous:" +  previous.getEntityId());
        System.out.println("previous:" +  previous.getState());


        //Then
        Assertions.assertEquals(expectedLogEntry, previous, "Should be equal because entityIDs are equal, states are different and logEntry occurred before explanandum");

    }
}

