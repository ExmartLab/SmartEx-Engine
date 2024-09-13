package exenginetest.engineservice;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.ErrorRepository;
import exengine.database.RuleRepository;
import exengine.datamodel.*;
import exengine.engineservice.CounterfactualExplanationService;
import exenginetest.algorithmicexplanationgenerator.TestingObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@DisplayName("Unit Test Counterfactual Explanation")
@ExtendWith(MockitoExtension.class)
public class CounterfactualExplanationUnitTest {

    TestingObjects testingObjects;

    @InjectMocks
    CounterfactualExplanationService underTest;

    @Mock
    private FindCauseService findCauseSer;


    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        testingObjects = new TestingObjects();
        underTest = new CounterfactualExplanationService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPreviousLogEntry() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        LogEntry explanandum = logEntries.get(7);
        LogEntry expectedLogEntry = logEntries.get(6);

        //When
        LogEntry previous = underTest.getPreviousLogEntry(explanandum, logEntries);


        //Then
        Assertions.assertEquals(expectedLogEntry, previous, "Should be equal because entityIDs are equal, states are different and logEntry occurred before explanandum");

    }

  /**  @Test
    void testModify() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry precondition = logEntries.get(7);
        ArrayList<LogEntry> expectedminPreconditions = logEntries;

        //When
        ArrayList<LogEntry> minPreconditions = underTest.modify(precondition, logEntries, dbRules);


        //Then
        Assertions.assertEquals(minPreconditions, expectedminPreconditions);

    }*/

  @Test
    void testGetCurrentState(){

      //Given
      ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
      ArrayList<LogEntry> expectedStates = new ArrayList<>();
      int[] iterator = {10,9,8,7,5,4,3,2,1};
      for (int i : iterator) {
          expectedStates.add(logEntries.get(i));
      }

      //When
      ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);

      //Then
      Assertions.assertEquals( expectedStates, currentState);
  }

  @Test
    void testHasTruePreconditions(){

      //Given
      ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
      ArrayList<Rule> dbRules = testingObjects.getDBRules();
      LogEntry explanandum = logEntries.get(10);
      Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class)))
              .thenReturn(true);

      //When
      Boolean truePreconditions = underTest.hasTruePreconditions(dbRules.get(5),explanandum, logEntries );
      Boolean falsePreconditions = underTest.hasTruePreconditions(dbRules.get(6),explanandum, logEntries );

      //Then
      Assertions.assertTrue(truePreconditions, "Should be true because rule " + dbRules.get(5).getRuleName() + " has true preconditions.");
      Assertions.assertFalse(falsePreconditions, "Should be false because rule " + dbRules.get(6).getRuleName() + " has false preconditions.");
  }

    @Test
    void testGetRulesTruePreconditions(){

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> toTest = new ArrayList<>();
        toTest.add(dbRules.get(5));
        toTest.add(dbRules.get(6));
        LogEntry explanandum = logEntries.get(10);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class)))
                .thenReturn(true);
        ArrayList<Rule> trueRules = new ArrayList<>();
        trueRules.add(dbRules.get(5));

        //When
        ArrayList<Rule> haveTruePreconditions = underTest.getRulesTruePreconditions(toTest, explanandum, logEntries );


        //Then
        Assertions.assertEquals( trueRules, haveTruePreconditions);
   }



}

