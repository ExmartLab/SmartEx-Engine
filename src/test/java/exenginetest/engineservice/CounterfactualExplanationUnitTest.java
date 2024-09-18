package exenginetest.engineservice;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
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
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;

@DisplayName("Unit Test Counterfactual Explanation")
@ExtendWith(MockitoExtension.class)
public class CounterfactualExplanationUnitTest {

    TestingObjects testingObjects;

    @InjectMocks
    CounterfactualExplanationService underTest;

    @Mock
    private FindCauseService findCauseSer;

    @Mock
    private DatabaseService dataSer;


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

  @Test
    void testGetCurrentState(){

      //Given
      ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
      ArrayList<LogEntry> expectedStates = new ArrayList<>();
      int[] iterator = {10,9,8,7,5,4,3,2,1};    /** These are the LogEntries that are currently true!*/
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
    void testTruePreconditions(){

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
        ArrayList<Rule> haveTruePreconditions = underTest.TruePreconditions(toTest, explanandum, logEntries );


        //Then
        Assertions.assertEquals( trueRules, haveTruePreconditions);
   }

   @Test
    void testModify() {

    //Given
    ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
    ArrayList<Rule> dbRules = testingObjects.getDBRules();
    LogEntry precondition = logEntries.get(10);
    //dbRules.get(7) and dbRules.get(8) can be fired to make precondition true
       //for each of the preconditions of the rules there are no other rules that can be fired to make them true
       //Therefore, the candidates are precondition, preconditions(dbRules.get(7)) and preconditions(dbRules.get(8))
       //Topsis currently returns the last option, so preconditions(dbRules.get(8)) is returned which has preconditions logEntries.get(0) and logEntries.get(5).
        //since makeFire only includes false preconditions, only expected.add(logEntries.get(0)) is returned
    ArrayList<LogEntry> expected = new ArrayList<>();
    expected.add(logEntries.get(0));
    Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);

    //When
    ArrayList<LogEntry> minPreconditions = underTest.modify(precondition, logEntries);


    //Then
    Assertions.assertEquals(minPreconditions, expected);
    }

    @Test
    void testMakeFire() {
    //Todo: Clean this method and implement another test which needs modify
        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<LogEntry> expected_9 = new ArrayList<>();
        ArrayList<LogEntry> expected_8 = new ArrayList<>();
        ArrayList<LogEntry> expected_7 = new ArrayList<>();
        expected_7.add(logEntries.get(0));
        expected_9.add(logEntries.get(0));
        expected_9.add(logEntries.get(6));
        expected_8.add(logEntries.get(0));
        expected_8.add(logEntries.get(4));
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        //System.out.println(dbRules.get(9).getRuleName());

        //When
        ArrayList<LogEntry> minPreconditions_7 = underTest.makeFire(dbRules.get(7), logEntries);
        ArrayList<LogEntry> minPreconditions_8 = underTest.makeFire(dbRules.get(8), logEntries);
        ArrayList<LogEntry> minPreconditions_9 = underTest.makeFire(dbRules.get(9), logEntries);
        //dbRules.get(9) has preconditions demoEntries.get(4), demoEntries.get(0), demoEntries.get(10)
        //0 , 4 are returned directly, while 10 has two rules that can fire to make it true: dbRules.get(7) and dbRules.get(8) (with ids 8,9)
        //7 has preconditions 0  and 5. makeFire(dbRules.get(7)) returns 0
        //8 has preconditions 0 and 4, which return 0 and 4
        //topsis calculates the minimum option for 10, which at the moment is 8, because it is considered last
        //makefire then return 0, 4, 0, 4


        //Then
        Assertions.assertEquals(expected_7,minPreconditions_7 );    //simple case without rules changing preconditions
        Assertions.assertEquals(expected_7,minPreconditions_8 );
        Assertions.assertEquals(expected_9,minPreconditions_9 );
    }





    @Test
    void testMinAdd(){

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();

        // Test with no rule to override:
        ArrayList<Rule> candidates = new ArrayList<>();
        candidates.add(dbRules.get(7));
        candidates.add(dbRules.get(8));
        Rule ruleToOverride = new Rule("dummy", null, null, null, null, null, null, 0);;
        LogEntry expected = logEntries.get(10);
        ArrayList<LogEntry> minAddExptected = new ArrayList<>();
        minAddExptected.add(logEntries.get(0));

        //8 is the only relevant rule, because the other will be overridden by topsis in the end
        //8 has preconditions 0 and 4, which only needs to fire 0

        Mockito.when(dataSer.findAllRules())
                .thenReturn(dbRules);
        Mockito.when(findCauseSer.findCandidateRules(any(LogEntry.class), anyList()))
                .thenReturn(candidates);

        //When
        ArrayList<LogEntry> minPreconditions = underTest.minAdd(ruleToOverride, expected, logEntries);

        //Then
        Assertions.assertEquals( minAddExptected, minPreconditions);
    }



}

