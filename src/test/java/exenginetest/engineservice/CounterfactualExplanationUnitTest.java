package exenginetest.engineservice;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.database.ErrorRepository;
import exengine.database.RuleRepository;
import exengine.datamodel.*;
import exengine.engineservice.ContrastiveExplanationService;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;

@DisplayName("Unit Test Counterfactual Explanation")
@ExtendWith(MockitoExtension.class)
public class CounterfactualExplanationUnitTest {

    TestingObjects testingObjects;

    @InjectMocks
    @Spy
    CounterfactualExplanationService underTest;

    @Mock
    private FindCauseService findCauseSer;

    @Mock
    private ContrastiveExplanationService contrastiveSer;

    @Mock
    private DatabaseService dataSer;


    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        testingObjects = new TestingObjects();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPreviousLogEntry() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        LogEntry explanandum = logEntries.get(22);
        LogEntry expectedLogEntry = logEntries.get(18);

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
      int[] iterator = {26,25,23,22,21,14,10,9,8,7,5,4,3,2,1};    //LogEntries that are currently true
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
      LogEntry explanandum_false = logEntries.get(15);
      LogEntry explanandum_true = logEntries.get(23);
      Mockito.when(findCauseSer.preconditionsApply(explanandum_false, dbRules.get(5), logEntries))
              .thenReturn(false);
      Mockito.when(findCauseSer.preconditionsApply(explanandum_true, dbRules.get(5), logEntries))
              .thenReturn(true);

      //When
      Boolean trueCondition_trueTrigger = underTest.hasTruePreconditions(dbRules.get(5),explanandum_true, currentState, logEntries );
      Boolean trueCondition_falseTrigger = underTest.hasTruePreconditions(dbRules.get(5),explanandum_false, currentState, logEntries );
      Boolean falsePreconditions = underTest.hasTruePreconditions(dbRules.get(6), explanandum_false, currentState, logEntries );

      //Then
      Assertions.assertTrue(trueCondition_trueTrigger, "Should be true because rule " + dbRules.get(5).getRuleName() + " has true preconditions and a true trigger.");
      Assertions.assertFalse(trueCondition_falseTrigger, "Should be false because rule " + dbRules.get(5).getRuleName() + " has true preconditions but no true trigger.");
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
        LogEntry explanandum = logEntries.get(23);
        ArrayList<Rule> trueRules = new ArrayList<>();
        trueRules.add(dbRules.get(5));
        Mockito.when(findCauseSer.preconditionsApply(explanandum, dbRules.get(5), logEntries))
                .thenReturn(true);
        Mockito.when(findCauseSer.preconditionsApply(explanandum, dbRules.get(6), logEntries))
                .thenReturn(true);

        //When
        underTest.TruePreconditions(toTest, explanandum,currentState, logEntries );


        //Then
        Assertions.assertEquals(trueRules, toTest);
   }

   @Test
    void testModify() {

    //Given
    ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
    ArrayList<Rule> dbRules = testingObjects.getDBRules();
    ArrayList<Rule> consideredRules = new ArrayList<>();
    for (int i = 5; i < dbRules.size(); i++){
        consideredRules.add(dbRules.get(i));
    }
    LogEntry precondition = logEntries.get(16);
    LogEntry explanandum = logEntries.get(20);


    Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
    Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any(ArrayList.class)))
               .thenAnswer(invocation -> {
                   ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                   ArrayList<LogEntry> dummy = new ArrayList<>();
                   for (ArrayList<LogEntry> candidate : candidates){
                       if (!candidate.isEmpty()){
                           dummy.add(candidate.get(0));
                       }
                   }
                   return dummy; // Return the first element of the input list
               });


    ArrayList<LogEntry> expectedCandidates = new ArrayList<>();
    expectedCandidates.add(logEntries.get(16));
    expectedCandidates.add(logEntries.get(18));
    expectedCandidates.add(logEntries.get(24));

    //When
    ArrayList<LogEntry> minPreconditions = underTest.modify(precondition, explanandum, logEntries);

    //Then
    Assertions.assertEquals(expectedCandidates, minPreconditions);
    }

    @Test
    void testMakeFire() {
        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> consideredRules = new ArrayList<>();
        for (int i = 5; i < dbRules.size(); i++){
            consideredRules.add(dbRules.get(i));
        }

        LogEntry explanandum = logEntries.get(15);
        ArrayList<LogEntry> expected_simple = new ArrayList<>();    //a simple case without any other rules
        ArrayList<LogEntry> expected_true = new ArrayList<>();      //a rule that is already active
        ArrayList<LogEntry> expected_complicated = new ArrayList<>();   //a rule that can be fired by using another rule
        expected_simple.add(logEntries.get(24));
        expected_complicated.add(logEntries.get(19));

        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any(ArrayList.class)))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates){
                        if (!candidate.isEmpty()){
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // Return the first element of the input list
                });


        //When
        ArrayList<LogEntry> minPreconditions_simple = underTest.makeFire(dbRules.get(8), explanandum,logEntries);
        ArrayList<LogEntry> minPreconditions_true = underTest.makeFire(dbRules.get(5), explanandum,logEntries);
        ArrayList<LogEntry> minPreconditions_complicated = underTest.makeFire(dbRules.get(7), explanandum,logEntries);


        //Then
        Assertions.assertEquals(expected_simple,minPreconditions_simple );
        Assertions.assertEquals(expected_true,minPreconditions_true );
        Assertions.assertEquals(expected_complicated,minPreconditions_complicated );
    }

    @Test
    void testMinAdd(){

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry explanandum = logEntries.get(13);

        ArrayList<Rule> candidateRules = new ArrayList<>();
        candidateRules.add(dbRules.get(6));
        candidateRules.add(dbRules.get(8));
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.findCandidateRules(any(LogEntry.class), anyList())).thenReturn(candidateRules);
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any(ArrayList.class)))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates){
                        if (!candidate.isEmpty()){
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // Return the first element of the input list
                });


        // Test with no rule to override:
        Rule ruleDummy = new Rule("dummy", null, null, null, null, null, null, 0);;
        LogEntry expected = logEntries.get(16);
        ArrayList<LogEntry> minAddExpected_1 = new ArrayList<>();
        minAddExpected_1.add(logEntries.get(18));
        minAddExpected_1.add(logEntries.get(24));

        //Test with rule to override:
        Rule ruleToOverride = dbRules.get(7);
        ArrayList<LogEntry> minAddExpected_2 = new ArrayList<>();
        minAddExpected_2.add(logEntries.get(24));       //dbRules.get(6) has lower priority, so it is not an option anymore



        //When
        ArrayList<LogEntry> minPreconditions_1 = underTest.minAdd(ruleDummy, explanandum, expected, logEntries);
        ArrayList<LogEntry> minPreconditions_2 = underTest.minAdd(ruleToOverride, explanandum, expected, logEntries);

        //Then
        Assertions.assertEquals( minAddExpected_1, minPreconditions_1);
        Assertions.assertEquals( minAddExpected_2, minPreconditions_2);
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
        //ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(11));
        candidate_1.add(logEntries.get(12));
       // candidate_2.add(logEntries.get(13));
        //candidate_2.add(logEntries.get(14));
        candidates.add(candidate_1);
        //candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(5.0);

        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class))).thenReturn(true);

        //When:
        ArrayList<Double> proximity = underTest.calculateProximity(candidates, explanandum,consideredLogEntries);

        //Then:
        Assertions.assertEquals(expected, proximity);
    }



}

