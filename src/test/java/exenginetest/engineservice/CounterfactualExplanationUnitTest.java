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
        LogEntry explanandum = logEntries.get(24);
        LogEntry expectedLogEntry = logEntries.get(20);

        //When
        LogEntry previous = underTest.getPreviousLogEntry(explanandum, logEntries);

        //Then
        Assertions.assertEquals(expectedLogEntry, previous, "Should be equal because entityIDs are equal, states are different and logEntry occurred before explanandum");

    }

    @Test
    void testGetCurrentState() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<LogEntry> expectedStates = new ArrayList<>();
        int[] indices = {28, 27, 25, 24, 23, 16, 12, 11, 10, 9, 8, 7, 5, 4, 3, 2, 1};    //LogEntries that are currently true
        for (int i : indices) {
            expectedStates.add(logEntries.get(i));
        }

        // When
        ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);

        // Then
        Assertions.assertEquals(expectedStates, currentState);
    }

    @Test
    void testHasTruePreconditions() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);
        LogEntry falseExplanandum = logEntries.get(17);
        LogEntry trueExplanandum = logEntries.get(25);

        // Mocks
        Mockito.when(findCauseSer.preconditionsApply(falseExplanandum, dbRules.get(5), logEntries))
                .thenReturn(false);
        Mockito.when(findCauseSer.preconditionsApply(trueExplanandum, dbRules.get(5), logEntries))
                .thenReturn(true);

        // When
        Boolean trueConditions_trueTrigger = underTest.hasTruePreconditions(dbRules.get(5), trueExplanandum, currentState, logEntries);
        Boolean trueConditions_falseTrigger = underTest.hasTruePreconditions(dbRules.get(5), falseExplanandum, currentState, logEntries);
        Boolean falseConditions = underTest.hasTruePreconditions(dbRules.get(6), falseExplanandum, currentState, logEntries);

        // Then
        Assertions.assertTrue(trueConditions_trueTrigger, "Should be true because rule " + dbRules.get(5).getRuleName() + " has true conditions and a true trigger.");
        Assertions.assertFalse(trueConditions_falseTrigger, "Should be false because rule " + dbRules.get(5).getRuleName() + " has true conditions but no true trigger.");
        Assertions.assertFalse(falseConditions, "Should be false because rule " + dbRules.get(6).getRuleName() + " has false conditions.");
    }

    @Test
    void testTruePreconditions() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> rulesToTest = new ArrayList<>();
        ArrayList<LogEntry> currentState = underTest.getCurrentState(logEntries);
        rulesToTest.add(dbRules.get(5));
        rulesToTest.add(dbRules.get(6));
        LogEntry explanandum = logEntries.get(25);

        // Expected
        ArrayList<Rule> trueRules = new ArrayList<>();
        trueRules.add(dbRules.get(5));

        // Mocks
        Mockito.when(findCauseSer.preconditionsApply(explanandum, dbRules.get(5), logEntries))
                .thenReturn(true);
        Mockito.when(findCauseSer.preconditionsApply(explanandum, dbRules.get(6), logEntries))
                .thenReturn(true);

        // When
        underTest.TruePreconditions(rulesToTest, explanandum, currentState, logEntries);

        // Then
        Assertions.assertEquals(trueRules, rulesToTest);
    }

    @Test
    void testModify() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> consideredRules = new ArrayList<>();
        for (int i = 5; i < dbRules.size(); i++) {
            consideredRules.add(dbRules.get(i));
        }

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // return the first element of the input list
                });


        // Expected
        ArrayList<LogEntry> expectedCandidates = new ArrayList<>();
        expectedCandidates.add(logEntries.get(18));
        expectedCandidates.add(logEntries.get(20));
        expectedCandidates.add(logEntries.get(26));

        // When
        ArrayList<LogEntry> minPreconditions = underTest.modify(logEntries.get(18), logEntries.get(22), logEntries);

        // Then
        Assertions.assertEquals(expectedCandidates, minPreconditions);
    }

    @Test
    void testMakeFire() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> consideredRules = new ArrayList<>();
        for (int i = 5; i < dbRules.size(); i++) {
            consideredRules.add(dbRules.get(i));
        }
        LogEntry explanandum = logEntries.get(17);

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(findCauseSer.preconditionsApply(any(), any(), any())).thenReturn(true);
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // return the first element of the input list
                });

        // Expected
        ArrayList<LogEntry> simpleExpected = new ArrayList<>();    // simple case without any other rules
        ArrayList<LogEntry> trueExpected = new ArrayList<>();      // rule that is already active
        ArrayList<LogEntry> trueComplicatedExpected = new ArrayList<>();   // rule that can be fired by using another rule
        ArrayList<LogEntry> falseComplicatedExpected = new ArrayList<>();
        simpleExpected.add(logEntries.get(26));
        falseComplicatedExpected.add(logEntries.get(20));
        falseComplicatedExpected.add(logEntries.get(21));


        // When
        ArrayList<LogEntry> minPreconditions_simple = underTest.makeFire(dbRules.get(8), explanandum, logEntries);
        ArrayList<LogEntry> minPreconditions_true = underTest.makeFire(dbRules.get(5), explanandum, logEntries);
        ArrayList<LogEntry> minPreconditions_true_complicated = underTest.makeFire(dbRules.get(7), explanandum, logEntries);
        ArrayList<LogEntry> minPreconditions_false_complicated = underTest.makeFire(dbRules.get(6), explanandum, logEntries);


        // Then
        Assertions.assertEquals(simpleExpected, minPreconditions_simple, "Should be equal because rule" + dbRules.get(8).getRuleName() + " has precondition " + logEntries.get(26).getName() + "and cannot be fired another way.");
        Assertions.assertEquals(new ArrayList<>(), minPreconditions_true, "Should be equal because rule" + dbRules.get(5).getRuleName() + "already has true preconditions.");
        Assertions.assertEquals(new ArrayList<>(), minPreconditions_true_complicated, "Should be equal because rule" + dbRules.get(7).getRuleName() + "already has true preconditions.");
        Assertions.assertEquals(falseComplicatedExpected, minPreconditions_false_complicated, "Should be equal because rule" + dbRules.get(6).getRuleName() + "can be fired by firing directly or by firing rule " + dbRules.get(9));
    }

    @Test
    void testMinAdd() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry explanandum = logEntries.get(15);
        LogEntry expected = logEntries.get(18);

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        ArrayList<Rule> candidateRules = new ArrayList<>();
        candidateRules.add(dbRules.get(6));
        candidateRules.add(dbRules.get(8));
        Mockito.when(findCauseSer.findCandidateRules(any(LogEntry.class), anyList())).thenReturn(candidateRules);
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // return the first element of the input list
                });

        // Expected
        // no rule to override:
        ArrayList<LogEntry> minAddExpected_1 = new ArrayList<>();
        minAddExpected_1.add(logEntries.get(20));
        minAddExpected_1.add(logEntries.get(26));
        //rule to override:
        ArrayList<LogEntry> minAddExpected_2 = new ArrayList<>();
        minAddExpected_2.add(logEntries.get(20));       //dbRules.get(6) has lower priority, so it is not an option anymore

        // When
        ArrayList<LogEntry> minPreconditions_1 = underTest.minAdd(new Rule("dummy", null, null, null, null, null, null, 0), explanandum, expected, logEntries);
        ArrayList<LogEntry> minPreconditions_2 = underTest.minAdd(dbRules.get(7), explanandum, expected, logEntries);

        // Then
        Assertions.assertEquals(minAddExpected_1, minPreconditions_1, "Should be equal because rule " + dbRules.get(6).getRuleName() + " and rule " + dbRules.get(9).getRuleName() + " can be fired.");
        Assertions.assertEquals(minAddExpected_2, minPreconditions_2, "Should be equal because rule " + dbRules.get(9).getRuleName() + " can be fired.");
    }


    @Test
    void testFindRoots() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        LogEntry explanandum = logEntries.get(28);

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.preconditionsApply(any(), any(), any())).thenReturn(true);
        LogEntry lampOn = logEntries.get(16);
        LogEntry fanOn = logEntries.get(18);
        LogEntry lampBlue = logEntries.get(20);
        LogEntry lampRed = logEntries.get(24);
        LogEntry fanOff = logEntries.get(25);
        Mockito.when(findCauseSer.findCandidateRules(any(), any()))
                .thenAnswer(invocation -> {
                    LogEntry input = invocation.getArgument(0);
                    if (input.equals(lampOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else if (input.equals(lampRed)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        return candidateRules;
                    } else if (input.equals(fanOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(6));
                        candidateRules.add(dbRules.get(8));
                        return candidateRules;
                    } else if (input.equals(fanOff)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(7));
                        return candidateRules;
                    } else if (input.equals(lampBlue)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else {
                        System.out.println("End of mock reached. No match found for " + input.getName());
                        return null;
                    }
                });

        // Expected
        ArrayList<LogEntry> expectedRoots = new ArrayList<>();
        expectedRoots.add(logEntries.get(27));
        expectedRoots.add(logEntries.get(23));

        // When
        ArrayList<LogEntry> simpleRoots = underTest.findRoots(lampOn, explanandum, logEntries);    //can be directly manipulated
        ArrayList<LogEntry> complicatedRoots = underTest.findRoots(fanOff, explanandum, logEntries);

        // Then
        Assertions.assertEquals(expectedRoots, simpleRoots, "Should be equal because" + dbRules.get(5).getRuleName() + " can be directly manipulated.");
        Assertions.assertEquals(expectedRoots, complicatedRoots, "Should be equal because" + dbRules.get(7).getRuleName() + " can be manipulated via rule" + dbRules.get(5).getRuleName());
    }

    @Test
    public void testMinSub() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any(ArrayList.class))).thenReturn(true);
        Mockito.when(dataSer.findEntityByEntityID(any())).thenReturn(new Entity("dummyId", "dummyDevice", "actionable"));
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // return the first element of the input list
                });
        LogEntry lampOn = logEntries.get(16);
        LogEntry fanOn = logEntries.get(18);
        LogEntry lampBlue = logEntries.get(20);
        LogEntry lampRed = logEntries.get(24);
        LogEntry fanOff = logEntries.get(25);
        Mockito.when(findCauseSer.findCandidateRules(any(), any()))
                .thenAnswer(invocation -> {
                    LogEntry input = invocation.getArgument(0);
                    if (input.equals(lampOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else if (input.equals(lampRed)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        return candidateRules;
                    } else if (input.equals(fanOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(6));
                        candidateRules.add(dbRules.get(8));
                        return candidateRules;
                    } else if (input.equals(fanOff)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(7));
                        return candidateRules;
                    } else if (input.equals(lampBlue)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else {
                        System.out.println("End of mock reached. No match found for " + input.getName());
                        return null;
                    }
                });

        // Expected
        ArrayList<LogEntry> minExpected = new ArrayList<>();
        minExpected.add(logEntries.get(27));
        minExpected.add(logEntries.get(23));

        // When
        ArrayList<LogEntry> minSub = underTest.minSub(dbRules.get(7), logEntries.get(25), logEntries);

        // Then
        Assertions.assertEquals(minExpected, minSub, "Should be equal because " +dbRules.get(7).getRuleName() + " can only be removed by removing "+ dbRules.get(5).getRuleName());
    }


    @Test
    void minSubAll() {

        //Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> rulesToReverse = new ArrayList<>();
        rulesToReverse.add(dbRules.get(7));
        rulesToReverse.add(dbRules.get(6));
        rulesToReverse.add(dbRules.get(5));

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any())).thenReturn(true);
        Mockito.when(dataSer.findEntityByEntityID(any())).thenReturn(new Entity("dummyId", "dummyDevice", "actionable"));
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // return the first element of the input list
                });

        LogEntry lampOn = logEntries.get(16);
        LogEntry fanOn = logEntries.get(18);
        LogEntry lampBlue = logEntries.get(20);
        LogEntry lampRed = logEntries.get(24);
        LogEntry fanOff = logEntries.get(25);
        Mockito.when(findCauseSer.findCandidateRules(any(), any()))
                .thenAnswer(invocation -> {
                    LogEntry input = invocation.getArgument(0);
                    if (input.equals(lampOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else if (input.equals(lampRed)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        return candidateRules;
                    } else if (input.equals(fanOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(6));
                        candidateRules.add(dbRules.get(8));
                        return candidateRules;
                    } else if (input.equals(fanOff)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(7));
                        return candidateRules;
                    } else if (input.equals(lampBlue)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else {
                        System.out.println("End of mock reached. No match found for " + input.getName());
                        return null;
                    }
                });

        // Expected
        ArrayList<LogEntry> minExpected = new ArrayList<>();
        minExpected.add(logEntries.get(27));
        minExpected.add(logEntries.get(23));

        // When
        ArrayList<LogEntry> minSub = underTest.minSubAll(rulesToReverse, logEntries.get(25), logEntries);

        // Then
        Assertions.assertEquals(minExpected, minSub, "Should be equal because rule " + dbRules.get(6).getRuleName() + " is already false and rule" + dbRules.get(7).getRuleName() + " only be removed by removing "+ dbRules.get(5).getRuleName() );
    }

    @Test
    void testOverrideOrRemove() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<Rule> dbRules = testingObjects.getDBRules();

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(dbRules);
        Mockito.when(findCauseSer.preconditionsApply(any(), any(), any())).thenReturn(true);
        Mockito.when(dataSer.findEntityByEntityID(any())).thenReturn(new Entity("dummyId", "dummyDevice", "actionable"));
        Mockito.when(underTest.minComputation(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    ArrayList<ArrayList<LogEntry>> candidates = invocation.getArgument(0);
                    ArrayList<LogEntry> dummy = new ArrayList<>();
                    for (ArrayList<LogEntry> candidate : candidates) {
                        if (!candidate.isEmpty()) {
                            dummy.add(candidate.get(0));
                        }
                    }
                    return dummy; // Return the first element of the input list
                });
        LogEntry lampOn = logEntries.get(16);
        LogEntry fanOn = logEntries.get(18);
        LogEntry lampBlue = logEntries.get(20);
        LogEntry lampRed = logEntries.get(24);
        LogEntry fanOff = logEntries.get(25);
        Mockito.when(findCauseSer.findCandidateRules(any(), any()))
                .thenAnswer(invocation -> {
                    LogEntry input = invocation.getArgument(0);
                    if (input.equals(lampOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else if (input.equals(lampRed)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(5));
                        return candidateRules;
                    } else if (input.equals(fanOn)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(6));
                        candidateRules.add(dbRules.get(8));
                        return candidateRules;
                    } else if (input.equals(fanOff)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(7));
                        return candidateRules;
                    } else if (input.equals(lampBlue)) {
                        ArrayList<Rule> candidateRules = new ArrayList<>();
                        candidateRules.add(dbRules.get(9));
                        return candidateRules;
                    } else {
                        System.out.println("End of mock reached. No match found for " + input.getName());
                        return null;
                    }
                });

        // Expected
        // additive and subtractive part
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(dbRules.get(7));
        ArrayList<ArrayList<LogEntry>> expectedMin = new ArrayList<>();
        ArrayList<LogEntry> additiveMin = new ArrayList<>();
        additiveMin.add(logEntries.get(20));
        ArrayList<LogEntry> subtractiveMin = new ArrayList<>();
        subtractiveMin.add(logEntries.get(27));
        expectedMin.add(additiveMin);
        expectedMin.add(subtractiveMin);
        //only subtractive part
        ArrayList<Rule> rulesSub = new ArrayList<>();
        rulesSub.add(dbRules.get(5));
        ArrayList<ArrayList<LogEntry>> expectedMinSub = new ArrayList<>();
        ArrayList<LogEntry> subtractiveMinSub = new ArrayList<>();
        subtractiveMinSub.add(logEntries.get(27));
        expectedMinSub.add(new ArrayList<>());
        expectedMinSub.add(subtractiveMinSub);

        // When
        ArrayList<ArrayList<LogEntry>> minSub = underTest.overrideOrRemove(rulesSub, logEntries.get(18), logEntries.get(25), false, logEntries);
        ArrayList<ArrayList<LogEntry>> min = underTest.overrideOrRemove(rules, logEntries.get(25), logEntries.get(18), false, logEntries);

        // Then
        Assertions.assertEquals(expectedMinSub, minSub, "Should be equal because " + dbRules.get(5).getRuleName() + " cannot be overridden and only directly reversed.");
        Assertions.assertEquals(expectedMin, min, "Should be equal because " + dbRules.get(7).getRuleName() + " can be overridden by " + dbRules.get(6).getRuleName() + " and reversed by reversing rule " + dbRules.get(7).getRuleName());


    }

    @Test
    void testCalculateSparsity() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(12));
        candidate_1.add(logEntries.get(14));
        candidate_1.add(logEntries.get(18));
        candidate_2.add(logEntries.get(15));
        candidate_2.add(logEntries.get(20));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        // Expected
        ArrayList<Double> expected = new ArrayList<>();
        expected.add(3.0);
        expected.add(2.0);

        // When
        ArrayList<Double> sparsity = underTest.calculateSparsity(candidates);

        // Then
        Assertions.assertEquals(expected, sparsity, "Should be equal because the first set contains 3 and the second set contains 3 entries.");
    }

    @Test
    void testCalculateAbnormality() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(13));
        candidate_1.add(logEntries.get(14));
        candidate_1.add(logEntries.get(18));
        candidate_2.add(logEntries.get(15));
        candidate_2.add(logEntries.get(20));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        // Expected
        ArrayList<Double> expected = new ArrayList<>();
        expected.add(41.666666666666664);
        expected.add(29.166666666666664);

        // When
        ArrayList<Double> abnormality = underTest.calculateAbnormality(candidates, logEntries);

        // Then
        Assertions.assertEquals(expected, abnormality, "Should be equal because for the first entry status.lamp has 2 states, color.lamp has 3 states and status.fan has 2 states. For the second entry setting.aircon has 3 states and color.lamp has 4 states." );
    }

    @Test
    void testCalculateTemporality() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(13));
        candidate_1.add(logEntries.get(14));
        candidate_1.add(logEntries.get(19));
        candidate_2.add(logEntries.get(15));
        candidate_2.add(logEntries.get(24));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        ArrayList<Double> expected = new ArrayList<>();
        expected.add(17.333333333333332);
        expected.add((double) Integer.MAX_VALUE /2);

        // When
        ArrayList<Double> abnormality = underTest.calculateTemporality(candidates, logEntries.get(23), logEntries);

        // Then
        Assertions.assertEquals(expected, abnormality, "Should be equal because " + logEntries.get(19) + " has an identical logEntry later and " + logEntries.get(24) + " is after the explanandum.");
    }

    @Test
    void testCalculateProximity() {

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        ArrayList<LogEntry> consideredLogEntries = new ArrayList<>();
        for (int i = 13; i < logEntries.size(); i++) {
            consideredLogEntries.add(logEntries.get(i));
        }
        ArrayList<Rule> dbRules = testingObjects.getDBRules();
        ArrayList<Rule> consideredRules = new ArrayList<>();
        for (int i = 5; i< dbRules.size(); i++){
            consideredRules.add(dbRules.get(i));
        }
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> candidate_1 = new ArrayList<>();
        ArrayList<LogEntry> candidate_2 = new ArrayList<>();
        candidate_1.add(logEntries.get(26));
        candidate_2.add(logEntries.get(21));
        candidates.add(candidate_1);
        candidates.add(candidate_2);

        // Mocks
        Mockito.when(dataSer.findAllRules()).thenReturn(consideredRules);
        Mockito.when(findCauseSer.preconditionsApply(any(LogEntry.class), any(Rule.class), any())).thenReturn(true);

        // Expected
        ArrayList<Double> expected = new ArrayList<>();
        expected.add(1.0);
        expected.add(3.0);

        // When
        ArrayList<Double> proximity = underTest.calculateProximity(candidates, logEntries.get(28), consideredLogEntries);

        // Then
        Assertions.assertEquals(expected, proximity);
    }

    @Test
    void testGenerateCFE() {
        //Todo: Improve generateCFE

        // Given
        ArrayList<LogEntry> logEntries = testingObjects.getDemoEntries();
        LogEntry explanandum = logEntries.get(18);
        LogEntry expected = logEntries.get(25);
        ArrayList<ArrayList<LogEntry>> minPreconditions = new ArrayList<>();
        ArrayList<LogEntry> add = new ArrayList<>();
        ArrayList<LogEntry> sub = new ArrayList<>();
        add.add(logEntries.get(16));
        add.add(logEntries.get(17));
        add.add(logEntries.get(19));
        sub.add(logEntries.get(28));
        minPreconditions.add(add);
        minPreconditions.add(sub);

        // When
        String explanation = underTest.generateCFE(minPreconditions, explanandum, expected);

        // Then
        Assertions.assertEquals("Fan turning off would have occurred instead of fan turning on if in the past lamp office turning on, lamp office turning yellow, and window closed had happened and and window closedhad not happened.", explanation);

    }


}

