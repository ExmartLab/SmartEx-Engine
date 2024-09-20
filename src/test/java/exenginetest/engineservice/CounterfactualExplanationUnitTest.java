package exenginetest.engineservice;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.database.ErrorRepository;
import exengine.database.RuleRepository;
import exengine.datamodel.*;
import exengine.engineservice.CounterfactualExplanationService;
import exenginetest.algorithmicexplanationgenerator.TestingObjects;
import org.apache.juli.logging.Log;
import org.junit.jupiter.api.*;

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
//@ExtendWith(MockitoExtension.class)
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
        LogEntry explanandum = logEntries.get(13);
        LogEntry expectedLogEntry = logEntries.get(11);

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
      int[] iterator = {21,20,15,13,10,9,8,7,5,4,3,2,1};    //LogEntries that are currently true
      for (int i : iterator) {
          expectedStates.add(logEntries.get(i));
      }

      //When
      ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);

      //Then
      Assertions.assertEquals(expectedStates, currentState);
  }

  @Test
    void testHasTruePreconditions(){

      //Given
      ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
      ArrayList<Rule> dbRules = testingObjects.getDBRules();
      ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);
      LogEntry explanandum = logEntries.get(17);
      Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class)))
              .thenReturn(true);

      //When
      Boolean truePreconditions = underTest.hasTruePreconditions(dbRules.get(5),explanandum, currentState, logEntries );
      Boolean falsePreconditions = underTest.hasTruePreconditions(dbRules.get(6), explanandum, currentState, logEntries );

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
        ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);
        toTest.add(dbRules.get(5));
        toTest.add(dbRules.get(6));
        LogEntry explanandum = logEntries.get(10);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class)))
                .thenReturn(true);
        ArrayList<Rule> trueRules = new ArrayList<>();
        trueRules.add(dbRules.get(5));

        //When
        ArrayList<Rule> haveTruePreconditions = underTest.TruePreconditions(toTest, explanandum,currentState, logEntries );


        //Then
        Assertions.assertEquals(trueRules, haveTruePreconditions);
   }

   @Test
    void testModify() {

    //Given
    ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
    ArrayList<Rule> dbRules = testingObjects.getDBRules();
    LogEntry precondition = logEntries.get(14);
    LogEntry explanandum = logEntries.get(11);

    Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);

    ArrayList<LogEntry> expectedCandidates = new ArrayList<>();
    expectedCandidates.add(logEntries.get(14));
    expectedCandidates.add(logEntries.get(16));

    //When
    ArrayList<LogEntry> minPreconditions = underTest.modify(precondition, explanandum, logEntries);

    //Then
    Assertions.assertEquals(expectedCandidates, minPreconditions);
    }

    @Test
    void testMakeFire() {
    //Todo: Clean this method and implement another test which needs modify
        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry explanandum = logEntries.get(6);
        ArrayList<LogEntry> expected_simple = new ArrayList<>();    //a simple case without any other rules
        ArrayList<LogEntry> expected_true = new ArrayList<>();      //a rule that is already active
        ArrayList<LogEntry> expected_complicated = new ArrayList<>();   //a rule that can be fired using another rule
        expected_simple.add(logEntries.get(11));
        expected_simple.add(logEntries.get(12));
        expected_complicated.add(logEntries.get(14));
        expected_complicated.add(logEntries.get(16));
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);


        //When
        ArrayList<LogEntry> minPreconditions_simple = underTest.makeFire(dbRules.get(7), explanandum,logEntries);
        ArrayList<LogEntry> minPreconditions_true = underTest.makeFire(dbRules.get(5), explanandum,logEntries);
        ArrayList<LogEntry> minPreconditions_complicated = underTest.makeFire(dbRules.get(8), explanandum,logEntries);


        //Then
        Assertions.assertEquals(expected_simple,minPreconditions_simple );
        Assertions.assertEquals(expected_true,minPreconditions_true );
        Assertions.assertEquals(expected_complicated,minPreconditions_complicated );
    }

    @Test
    @Disabled
    void testMinAdd(){

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry explanandum = logEntries.get(13);

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

        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.findCandidateRules(any(LogEntry.class), anyList())).thenReturn(candidates);

        //When
        ArrayList<LogEntry> minPreconditions = underTest.minAdd(ruleToOverride, explanandum, expected, logEntries);

        //Then
        Assertions.assertEquals( minAddExptected, minPreconditions);
    }


    @Test
    void testCalculateSparsity(){
        //Given:
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(11));
        candidate_1.add(logEntries.get(12));
        candidate_1.add(logEntries.get(16));
        candidate_2.add(logEntries.get(13));
        candidate_2.add(logEntries.get(14));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(3.0);
        expected.add(2.0);

        //When:
        ArrayList<Double> sparsity = underTest.calculateSparsity(candidates);

        //Then:
        Assertions.assertEquals(expected, sparsity);
    }

    @Test
    void testCalculateAbnormality(){
        //Given:
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(11));
        candidate_1.add(logEntries.get(12));
        candidate_1.add(logEntries.get(16));
        candidate_2.add(logEntries.get(13));
        candidate_2.add(logEntries.get(14));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(36.11111111111111);
        expected.add(41.666666666666664);

        //When:
        ArrayList<Double> abnormality = underTest.calculateAbnormality(candidates, logEntries);

        //Then:
        Assertions.assertEquals(expected, abnormality);
    }

    @Test
    void testCalculateTemporality(){
        //Given:
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(11));
        candidate_1.add(logEntries.get(12));
        candidate_1.add(logEntries.get(17));
        candidate_2.add(logEntries.get(13));
        candidate_2.add(logEntries.get(14));
        candidate_2.add(logEntries.get(21));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(15.0);
        expected.add(13.333333333333334);

        //When:
        ArrayList<Double> abnormality = underTest.calculateTemporality(candidates, logEntries.get(20),logEntries);

        //Then:
        Assertions.assertEquals(expected, abnormality);
    }

    @Test
    @Disabled
    void testCalculateProximity(){
        //Given:
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<LogEntry> consideredLogEntries = new ArrayList<>();
        for (int i = 11; i <22; i++){
            consideredLogEntries.add(logEntries.get(i));
        }
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> consideredRules = new ArrayList<>();
        consideredRules.add(dbRules.get(5));
        consideredRules.add(dbRules.get(6));
        consideredRules.add(dbRules.get(7));
        consideredRules.add(dbRules.get(8));

        LogEntry explanandum = logEntries.get(20);
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(11));
        //candidate_1.add(logEntries.get(12));
        candidate_2.add(logEntries.get(13));
        candidate_2.add(logEntries.get(14));
        candidates.add(candidate_1);
        //candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(13.666666666666666);
        expected.add(12.0);

        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class))).thenReturn(true);

        //When:
        ArrayList<Double> proximity = underTest.calculateProximity(candidates, explanandum,consideredLogEntries);

        //Then:
        Assertions.assertEquals(expected, proximity);
    }



}

