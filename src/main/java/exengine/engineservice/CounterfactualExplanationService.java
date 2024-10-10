package exengine.engineservice;

import java.util.*;
import java.time.LocalDateTime;

import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class CounterfactualExplanationService extends ExplanationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterfactualExplanationService.class);

    @Autowired
    FindCauseService findCauseSer;

    @Autowired
    DatabaseService dataSer;

    @Autowired
    ContrastiveExplanationService contrastiveSer;

    final Double ABNORMALITY_WEIGHT = 1.0;
    final Double TEMPORALITY_WEIGHT = 1.0;
    final Double PROXIMITY_WEIGHT = 1.0;
    final Double SPARSITY_WEIGHT = 1.0;

    final Boolean ABNORMALITY_BENEFICIAL = false;
    final Boolean TEMPORALITY_BENEFICIAL = false;
    final Boolean PROXIMITY_BENEFICIAL = false;
    final Boolean SPARSITY_BENEFICIAL = false;


    /**
     * determines a counterfactual explanation. It uses the Contrastive Explanation Service to determine the correct
     * foil and then finds the minimal change to the system (i.e. to the LogEntries) such that the determined foil would
     * have happened instead of the fact / explanandum.
     *
     * @param min    Representing the number of minutes taken into account for analyzing past events,
     *               starting from the call of the method
     * @param userId the user identifier for the explainee that asked for the explanation
     * @param device the device whose last action is to be explained
     * @return either the built explanation, or an error description in case the explanation could not be built.
     */
    @Override
    public String getExplanation(int min, String userId, String device) {
        LOGGER.debug("getExplanation (counterfactual) called with arguments min: {}, user id: {}, device: {}", min, userId, device);

        ArrayList<LogEntry> logEntries = getLogEntries(min);
        List<Rule> dbRules = dataSer.findAllRules();

        // Determine explanandum / current state of the device of interest
        LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);
        if (explanandum == null || explanandum.getEntityId() == null || explanandum.getState() == null) {
            return "No explanandum found. Could not proceed.";
        }
        LOGGER.info("Found explanandum: {}", explanandum.getName());
        LOGGER.info("Found current state: {}", explanandum.getState());


        // Determine previous state of the device of interest
        LogEntry previous = getPreviousLogEntry(explanandum, logEntries); // = explanandum if there is none
        LOGGER.info("Found previous: {}", previous.getName());
        LOGGER.info("Found previous state: {}", previous.getState());


        // Determine expected state of the device of interest
        LogEntry expected = determineFoil(explanandum, logEntries, device, userId);
        LOGGER.info("Found expected: {}", expected.getName());
        LOGGER.info("Found expected state: {}", expected.getState());

        if (explanandum.equals(expected)) {
            return "Error, there is no explanation need. Fact and Foil are the same.";
        }

        // Determine rules with currently true preconditions
        ArrayList<LogEntry> currentState = getCurrentState(logEntries);

        // rules with actions leading to current state
        ArrayList<Rule> rulesCurrent = findCauseSer.findCandidateRules(explanandum, dbRules);
        TruePreconditions(rulesCurrent, explanandum, currentState, logEntries);    //checks if the conditions currently are true and a trigger has been activated before the explanandum
        LOGGER.info("Rules with true preconditions leading to the current state are " + rulesCurrent);

        // rules with actions leading to expected state
        ArrayList<Rule> rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);
        TruePreconditions(rulesExpected, explanandum, currentState, logEntries);
        LOGGER.info("Rules with true preconditions leading to the expected state are " + rulesExpected);

        // rules with actions leading to previous state
        ArrayList<Rule> rulesPrevious = findCauseSer.findCandidateRules(previous, dbRules);
        TruePreconditions(rulesPrevious, explanandum, currentState, logEntries);
        if (!rulesExpected.isEmpty()) {         // Only consider rules with higher priority than rules in rulesExpected
            Rule maxPriorityRule = Collections.max(rulesExpected, Comparator.comparingInt(Rule::getPriority));
            int maxPriority = maxPriorityRule.getPriority();
            rulesPrevious.removeIf(r -> r.getPriority() <= maxPriority);
        }
        LOGGER.info("Rules with true preconditions leading to the previous state are " + rulesPrevious);


        // Case Distinction
        LOGGER.info("All rules with true preconditions are determined. Proceed with case distinction.");
        boolean firingNecessary = true;
        ArrayList<ArrayList<LogEntry>> minPreconditions = new ArrayList<>();    //first Array contains additive, second subtractive information
        if (!rulesCurrent.isEmpty()) {
            if (!rulesPrevious.isEmpty() || expected.equals(previous)) {
                firingNecessary = false;
            }
            rulesCurrent.addAll(rulesPrevious);
            noDuplicates(rulesCurrent);
            minPreconditions.addAll(overrideOrRemove(rulesCurrent, explanandum, expected, firingNecessary, logEntries));
        } else {
            if (rulesExpected.isEmpty()) {
                Rule dummy = new Rule("dummy", null, null, null, null, null, null, 0);
                LOGGER.info("There are no rules with true preconditions. A rule to fire is determined.");
                minPreconditions.add(minAdd(dummy, explanandum, expected, logEntries));
            } else {
                return "Error, there is no explanation need.";
            }
        }


        LOGGER.info("Minimal change determined.");
        return generateCFE(minPreconditions, explanandum, expected, device);

    }

    /**
     * Determines the expected situation / foil using the Contrastive Explanation Service
     *
     * @param explanandum the action that is to be explained
     * @param logEntries  the list of Home Assistant Logs
     * @param device      the device whose last action is to be explained
     * @param userId      the user identifier for the explainee that asked for the explanation
     * @return the LogEntry that is associated to the determined foil
     */
    public LogEntry determineFoil(LogEntry explanandum, ArrayList<LogEntry> logEntries, String device, String userId) {

        List<Rule> dbRules = dataSer.findAllRules();
        List<Error> dbErrors = dataSer.findAllErrors();
        String entityId = explanandum.getEntityId();
        LogEntry expected = explanandum;

        // determine foil using the ContrastiveExplanationService
        Object happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
        ArrayList<Rule> candidateRules = new ArrayList<>(contrastiveSer.getCandidateRules(entityId));

        while (expected.equals(explanandum)){       //exclude determined expectedRule until we reach foil that is not the explanandum
            Rule expectedRule = contrastiveSer.getExpectedRule(explanandum, logEntries, candidateRules, happenedEvent, device, entityId, userId);
            if (expectedRule == null) {
                return explanandum;
            }
            ArrayList<LogEntry> expectedCandidates = expectedRule.getActions();
            for (LogEntry logEntry : expectedCandidates) {
                if (logEntry.getEntityId().equals(entityId)) {
                    expected = logEntry;
                }
            }
            candidateRules.remove(expectedRule);
        }

        return expected;
}


    /**
     * transforms the determined minimal change into a counterfactual explanation
     *
     * @param minPreconditions the determined minimal change to the system to achieve the foil instead of the fact
     * @param explanandum      the situation that is to be explained
     * @param expected         the situation the user expected instead
     * @param device           the device whose last action is to be explained
     * @return a counterfactual explanation containing the minimal change
     */
    public String generateCFE(ArrayList<ArrayList<LogEntry>> minPreconditions, LogEntry explanandum, LogEntry expected, String device) {

        String explanation = "The " + device + " would be " + expected.getState() + " instead of " + explanandum.getState() + " if in the past ";

        // add additive preconditions to explanation
        ArrayList<LogEntry> addPreconditions = minPreconditions.get(0);
        for (int i = 0; i < addPreconditions.size() - 1; i++) {
            String deviceAdd = dataSer.findEntityByEntityID(addPreconditions.get(i).getEntityId()).getDeviceName();
            explanation = explanation.concat("the " + deviceAdd + " was " + addPreconditions.get(i).getState() + ", ");
        }

        if (addPreconditions.size() > 0) {
            if (addPreconditions.size() >= 2) {
                explanation = explanation.concat("and ");
            }
            String deviceAdd = dataSer.findEntityByEntityID(addPreconditions.get(addPreconditions.size() - 1).getEntityId()).getDeviceName();
            explanation = explanation.concat("the " + deviceAdd + " was " + addPreconditions.get(addPreconditions.size() - 1).getState());
        }

        // add subtractive preconditions to explanation
        if (minPreconditions.size() > 1 && minPreconditions.get(1).size() > 0) {
            ArrayList<LogEntry> subPreconditions = minPreconditions.get(1);
            if (addPreconditions.size() > 0) {
                explanation = explanation.concat(" and ");
            }
            for (int i = 0; i < subPreconditions.size() - 1; i++) {
                String deviceSub = dataSer.findEntityByEntityID(subPreconditions.get(i).getEntityId()).getDeviceName();
                explanation = explanation.concat("the " + deviceSub + " was not " + subPreconditions.get(i).getState() + ", ");
            }

            if (subPreconditions.size() > 0) {
                if (subPreconditions.size() >= 2) {
                    explanation = explanation.concat("and ");
                }
                String deviceSub = dataSer.findEntityByEntityID(subPreconditions.get(subPreconditions.size() - 1).getEntityId()).getDeviceName();
                explanation = explanation.concat("the " + deviceSub + " was not " + subPreconditions.get(subPreconditions.size() - 1).getState());
            }
        }
        explanation = explanation.concat(".");

        // Capitalize only the first letter
        explanation = explanation.toLowerCase();
        explanation = explanation.substring(0, 1).toUpperCase() + explanation.substring(1);

        LOGGER.info("Explanation generated.");
        return explanation;
    }

    // Methods to determine all possibilities to achieve the foil:

    /**
     * Finds the minimal set of LogEntries to change such that ruleToReverse does not have true preconditions anymore.
     *
     * @param ruleToReverse the rule that is to be reversed
     * @param explanandum   the action that is to be explained
     * @param logEntries    the list of Home Assistant logs
     * @return the minimal set of LogEntries s.t. reversing them prevents ruleToReverse from being active
     * or null if there is no such set
     */
    public ArrayList<LogEntry> minSub(Rule ruleToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {


        ArrayList<ArrayList<LogEntry>> reversibleCandidates = new ArrayList<>();
        ArrayList<LogEntry> conditions = ruleToReverse.getConditions();
        ArrayList<LogEntry> actionableConditions = new ArrayList<>();
        ArrayList<LogEntry> mutableConditions = new ArrayList<>();
        ArrayList<LogEntry> nonMutableConditions = new ArrayList<>();

        // Sort by controllability
        for (LogEntry condition : conditions) {
            String controllability = getControllabilityByEntityId(condition.getEntityId());
            switch (controllability) {
                case "actionable" -> actionableConditions.add(condition);
                case "mutable" -> mutableConditions.add(condition); // mutable but non-actionable
                case "non-mutable" -> nonMutableConditions.add(condition);
                default -> {
                    LOGGER.info("An entity has invalid controllability. It is removed from the set of options to reverse.");
                }
            }
        }

        // actionable preconditions
        for (LogEntry condition : actionableConditions) {
            reversibleCandidates.addAll(findRoots(condition, explanandum, logEntries));
        }

        // mutable but non-actionable preconditions
        for (LogEntry condition : mutableConditions) {
            reversibleCandidates.addAll(findRoots(condition, explanandum, logEntries));
            reversibleCandidates.remove(new ArrayList<>(Collections.singletonList(condition))); // precondition cannot be directly manipulated
        }

        // consider non-mutable preconditions if there are no other ones
        if (reversibleCandidates.isEmpty()) {
            for (LogEntry condition : nonMutableConditions) {
                reversibleCandidates.add(new ArrayList<>(Collections.singletonList(condition)));
            }
        }

        noDuplicates(reversibleCandidates);

        // findRoots considers rules with true preconditions
        // but the manipulation can use a rule that that does not have true preconditions
        ArrayList<ArrayList<LogEntry>> modifiedCandidates = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : reversibleCandidates) {
            ArrayList<LogEntry> modified = new ArrayList<>();
            for (LogEntry c : candidate) {
                modified.addAll(modify(c, explanandum, logEntries));
            }
            modifiedCandidates.add(modified);
        }

        // reverse triggers
        ArrayList<LogEntry> currentState = getCurrentState(logEntries);
        ArrayList<LogEntry> triggers = ruleToReverse.getTrigger();

        // only consider true triggers
        triggers.removeIf(trigger -> !currentState.contains(trigger));

        //add all options to remove all triggers
        ArrayList<ArrayList<LogEntry>> allRoots = new ArrayList<>();
        for (LogEntry trigger : triggers) {
            allRoots.addAll(findRoots(trigger, explanandum, logEntries));
        }

        // find combinations s.t. for each trigger one root is chosen
        generatePermutations(allRoots, new ArrayList<>(), 0, new ArrayList<>());
        modifiedCandidates.addAll(allRoots);

        return minComputation(modifiedCandidates, explanandum, logEntries);
    }

    /**
     * Finds the minimal set of LogEntries to change s.t. all rules in rulesToReverse do not have true
     * preconditions anymore.
     *
     * @param rulesToReverse the rules that are to be reversed
     * @param explanandum    the action that is to be explained
     * @param logEntries     the list of Home Assistant logs
     * @return the minimal set of LogEntries s.t. reversing them prevents all rules in rulesToReverse from being active
     * or null if there is no such set
     */
    public ArrayList<LogEntry> minSubAll(ArrayList<Rule> rulesToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        ArrayList<LogEntry> conditions = new ArrayList<>();
        for (Rule r : rulesToReverse) {
            ArrayList<LogEntry> sub = minSub(r, explanandum, logEntries);
            if (sub == null) {   // there is an invalid entry
                return null;
            }
            conditions.addAll(sub);
        }
        noDuplicates(conditions);
        return conditions;
    }


    /**
     * Finds minimal set of LogEntries that need to true so that a rule is fired that overrides ruleToOverride.
     *
     * @param ruleToOverride the rule that is to be overridden
     * @param explanandum    the action of the device to be explained
     * @param toAchieve      the state of the device that is to be achieved
     * @param logEntries     the list of Home Assistant logs
     * @return minimal set of LogEntries s.t. if the System had these states of the devices ruleToOverride would be
     * overridden by another rule or null if there is no such set.
     */
    public ArrayList<LogEntry> minAdd(Rule ruleToOverride, LogEntry explanandum, LogEntry toAchieve, ArrayList<LogEntry> logEntries) {

        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<Rule> candidateRules = findCauseSer.findCandidateRules(toAchieve, dbRules);

        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();

        if (candidateRules != null && !candidateRules.isEmpty()) {

            for (Rule r : candidateRules) {

                // Find minimal way to make r fire
                ArrayList<LogEntry> candidate = makeFire(r, explanandum, logEntries);

                // only consider rules that override ruleToOverride and don't need too many changes
                if (r.getPriority() > ruleToOverride.getPriority() && candidate.size() <= 3) {
                    candidates.add(candidate);
                }
            }

        } else {
            LOGGER.info("No additive explanation available. No rule that could fire found.");
            return null;
        }

        return minComputation(candidates, explanandum, logEntries);
    }


    /**
     * determines the minimal change to invalidate the rules by removing some and overriding others by firing new rules.
     *
     * @param rules           the rules to invalidate
     * @param explanandum     the situation to be explained
     * @param expected        the situation the user expects instead
     * @param firingNecessary if it is necessary to fire a new rule or if reversing all rules is enough
     * @param logEntries      the list of Home Assistant Logs
     * @return the minimal change to the system s.t. the rules are not in effect anymore.
     */
    public ArrayList<ArrayList<LogEntry>> overrideOrRemove(ArrayList<Rule> rules, LogEntry explanandum, LogEntry expected, Boolean firingNecessary, ArrayList<LogEntry> logEntries) {

        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> addCandidates = new ArrayList<>();  // collect additive candidates
        ArrayList<LogEntry> subCandidates = new ArrayList<>();  // collect subtractive candidates


        // Sort the ArrayList by priority in descending order:
        ArrayList<Rule> rulesSorted = new ArrayList<>(rules);
        rulesSorted.sort((r1, r2) -> {
            // Sort in descending order
            return Integer.compare(r2.getPriority(), r1.getPriority());
        });

        // determine minimal change if the cut-off point is at point r:
        // All rules with higher priority are reversed, all rules with lower priority (including r)are overridden
        for (int i = 0; i < rulesSorted.size(); i++) {
            Rule r = rulesSorted.get(i);
            ArrayList<LogEntry> candidate = new ArrayList<>();

            //additive part:
            ArrayList<LogEntry> overriding = minAdd(r, explanandum, expected, logEntries);

            //subtractive part:
            ArrayList<LogEntry> subtracting = new ArrayList<>();
            if (i != 0) {    //r has not the highest priority, i.e. there is a subtractive part
                ArrayList<Rule> rulesHigherPriority = new ArrayList<>(rulesSorted.subList(0, i - 1));
                subtracting = minSubAll(rulesHigherPriority, explanandum, logEntries);
            }

            // add to candidate
            if (subtracting != null && overriding != null) {    // candidate is valid
                if (!overriding.isEmpty()) {
                    candidate.addAll(overriding);
                    addCandidates.addAll(overriding);
                }
                if (!subtracting.isEmpty()) {
                    candidate.addAll(subtracting);
                    subCandidates.addAll(candidate);
                }
                if (!candidate.isEmpty()) {
                    candidates.add(candidate);
                    LOGGER.info("Candidate for minimum added:" + candidate);
                }

            }
        }

        // removing all rules
        ArrayList<LogEntry> subtractingAll = minSubAll(rulesSorted, explanandum, logEntries);
        subCandidates.addAll(subtractingAll);
        if (firingNecessary) {
            Rule dummy = new Rule("dummy", null, null, null, null, null, null, 0);
            ArrayList<LogEntry> additivePart = minAdd(dummy, explanandum, expected, logEntries);
            addCandidates.addAll(additivePart);
            subtractingAll.addAll(additivePart);
        }
        LOGGER.info("Candidate for minimum added:" + subtractingAll);
        candidates.add(subtractingAll);

        // minimal computation
        ArrayList<LogEntry> mixedMin = minComputation(candidates, explanandum, logEntries);

        // determine which were additive or subtractive:
        ArrayList<LogEntry> addMin = new ArrayList<>();
        ArrayList<LogEntry> subMin = new ArrayList<>();
        for (LogEntry entry : mixedMin) {
            if (addCandidates.contains(entry)) {
                addMin.add(entry);
            }
            if (subCandidates.contains(entry)) {
                subMin.add(entry);
            }
        }
        ArrayList<ArrayList<LogEntry>> sortedMin = new ArrayList<>();
        sortedMin.add(addMin);
        sortedMin.add(subMin);

        return sortedMin;
    }


    /**
     * finds all preconditions (i.e. Logentries) that can be directly manipulated to make precondition c false.
     * c may not be able to be manipulated directly because there is a rule with true preconditions that would change
     * c back immediately.
     *
     * @param c           the LogEntry that needs to be manipulated
     * @param explanandum the action to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return all preconditions that can be directly manipulated to make precondition c false (including c if c can be
     * directly manipulated) or an empty array if c is already true
     */
    public ArrayList<ArrayList<LogEntry>> findRoots(LogEntry c, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> currentState = getCurrentState(logEntries);
        if (!currentState.contains(c)) {      //c is already false, nothing has to be changed
            return new ArrayList<>();
        }


        // determine rules that prevent us from directly changing c
        ArrayList<Rule> disturbingRules = findCauseSer.findCandidateRules(c, dbRules);
        TruePreconditions(disturbingRules, explanandum, currentState, logEntries);


        ArrayList<ArrayList<LogEntry>> changeablePreconditions = new ArrayList<>();
        if (disturbingRules.isEmpty()) {  // can directly change c
            changeablePreconditions.add(new ArrayList<>(Collections.singletonList(c)));
        } else {

            for (Rule r : disturbingRules) {

                // find roots for each condition (changing one root is enough to make r not have true preconditions)
                ArrayList<LogEntry> conditions = r.getConditions();
                for (LogEntry condition : conditions) {
                    ArrayList<ArrayList<LogEntry>> conditionRoots = findRoots(condition, explanandum, logEntries);
                    changeablePreconditions.addAll(conditionRoots);
                }

                //find roots for triggers (all true triggers must be changed to make r not have a true trigger)
                ArrayList<LogEntry> triggers = new ArrayList<>(r.getTrigger());
                triggers.removeIf(trigger -> !currentState.contains(trigger));  // only consider true triggers

                ArrayList<ArrayList<LogEntry>> triggerRoots = new ArrayList<>();
                for (LogEntry trigger : triggers) {
                    triggerRoots.addAll(findRoots(trigger, explanandum, logEntries));
                }
                generatePermutations(triggerRoots, new ArrayList<>(), 0, new ArrayList<>());
                changeablePreconditions.addAll(triggerRoots);

            }
        }

        noDuplicates(changeablePreconditions);
        return changeablePreconditions;

    }

    /**
     * finds the minimal set of Logentries with states the system should have to make the precondition true.
     *
     * @param precondition the condition or trigger of a rule that should be modified
     * @param logEntries   the list of Home Assistant logs
     * @return minimal set of LogEntries with states the system should have to make the precondition true
     */
    public ArrayList<LogEntry> modify(LogEntry precondition, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();

        // changing precondition directly is possible
        minCandidates.add(new ArrayList<>(Collections.singletonList(precondition)));


        // find rules that can change the precondition
        for (Rule r : dbRules) {
            if (r.getActions().contains(precondition)) {
                minCandidates.add(makeFire(r, explanandum, logEntries));    //minimal changes to make r fire
            }
        }

        return minComputation(minCandidates, explanandum, logEntries);
    }

    /**
     * Determines the states and EntityIds that need to be changed to make rule r have true preconditions
     * (conditions and triggers).
     *
     * @param r           the rule that should have true preconditions
     * @param explanandum the action of the device that is to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return the list of LogEntries which contain the states the system has to have to make r have true preconditions
     */
    public ArrayList<LogEntry> makeFire(Rule r, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<LogEntry> currentState = getCurrentState(logEntries);
        ArrayList<LogEntry> conditions = new ArrayList<>(r.getConditions());
        ArrayList<LogEntry> triggers = new ArrayList<>(r.getTrigger());

        // remove invalid and true conditions of r
        Iterator<LogEntry> i = conditions.iterator();
        while (i.hasNext()) {
            LogEntry p = i.next();
            if (p == null || p.getEntityId() == null || p.getState() == null) {   // remove invalid conditions
                i.remove();
            } else {
                for (LogEntry state : currentState) {  //remove true preconditions
                    if (p.equals(state)) {
                        i.remove();
                    }
                }
            }
        }

        // check if it is more minimal to fire a rule instead of manipulating the condition directly
        ArrayList<LogEntry> modifiedConditions = new ArrayList<>();
        for (LogEntry condition : conditions) {
            ArrayList<LogEntry> modifiedCondition = modify(condition, explanandum, logEntries);
            if (!modifiedCondition.isEmpty()) {
                modifiedConditions.addAll(modifiedCondition);
            }
        }


        // check if r has a true trigger
        boolean hasTrueTrigger = true;
        if (explanandum.getTime() != null) {        // if explanandum has invalid time, assume the trigger is true
            hasTrueTrigger = findCauseSer.preconditionsApply(explanandum, r, logEntries);
        }
        if (!hasTrueTrigger) {
            ArrayList<ArrayList<LogEntry>> triggerCandidates = new ArrayList<>();
            for (LogEntry trigger : triggers) {

                // remove invalid triggers
                if (trigger == null || trigger.getEntityId() == null || trigger.getState() == null) {
                    i.remove();

                } else {

                    // check if it is more minimal to fire a rule instead of manipulating the trigger directly
                    ArrayList<LogEntry> triggerCandidate = modify(trigger, explanandum, logEntries);
                    if (!triggerCandidate.isEmpty()) {
                        triggerCandidates.add(triggerCandidate);
                    }
                }

            }

            // find minimal trigger
            ArrayList<LogEntry> minTrigger = minComputation(triggerCandidates, explanandum, logEntries);
            modifiedConditions.addAll(minTrigger);
        }

        return modifiedConditions;
    }


    // Methods to determine the minimum from all possibilities

    /**
     * determines the minimal change from the set of candidates using topsis and the properties abnormality,
     * temporality, proximity, and sparsity.
     *
     * @param candidates  the set of candidates from which the minimum is determined
     * @param explanandum the situation to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return the minimal candidate from the set of candidates
     */
    public ArrayList<LogEntry> minComputation(ArrayList<ArrayList<LogEntry>> candidates, LogEntry
            explanandum, ArrayList<LogEntry> logEntries) {

        if (candidates == null || candidates.isEmpty()) {
            LOGGER.info("No candidates to calculate the minimum found. Empty array is returned.");
            return new ArrayList<>();
        }

        // remove duplicates
        noDuplicates(candidates);

        // check for actionability
        ArrayList<ArrayList<LogEntry>> actionableCandidates = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : candidates) {
            actionableCandidates.add(candidate);
            for (LogEntry c : candidate) {  //candidate is not actionable if one precondition is not actionable
                if (!getControllabilityByEntityId(c.getEntityId()).equals("actionable")) {
                    actionableCandidates.remove(candidate);
                }
            }
        }

        // only consider actionable ones if possible
        if (!actionableCandidates.isEmpty()) {
            candidates = actionableCandidates;
        }

        // calculate properties
        ArrayList<Double> abnormality = calculateAbnormality(candidates, logEntries);
        ArrayList<Double> temporality = calculateAbnormality(candidates, logEntries);
        ArrayList<Double> proximity = calculateProximity(candidates, explanandum, logEntries);
        ArrayList<Double> sparsity = calculateSparsity(candidates);

        // define weights
        ArrayList<Double> weights = new ArrayList<>();
        weights.add(ABNORMALITY_WEIGHT);
        weights.add(TEMPORALITY_WEIGHT);
        weights.add(PROXIMITY_WEIGHT);
        weights.add(SPARSITY_WEIGHT);

        // define whether property is beneficial or not
        ArrayList<Boolean> isBeneficial = new ArrayList<>();
        isBeneficial.add(ABNORMALITY_BENEFICIAL);
        isBeneficial.add(TEMPORALITY_BENEFICIAL);
        isBeneficial.add(PROXIMITY_BENEFICIAL);
        isBeneficial.add(SPARSITY_BENEFICIAL);

        // calculate minimum using topsis
        ArrayList<LogEntry> minimum = ContrastiveExplanationService.topsis(candidates, weights, isBeneficial, abnormality,
                temporality, proximity, sparsity);

        if (minimum == null) {
            LOGGER.error("Error in TOPSIS Calculation");
            return new ArrayList<>();
        }

        return minimum;
    }


    // Measuring the properties

    /**
     * determines the sparsity in candidates, i.e. the amount of features to change
     *
     * @param candidates the set the sparsity is to be determined for
     * @return the size of the candidates array
     */
    public ArrayList<Double> calculateSparsity(ArrayList<ArrayList<LogEntry>> candidates) {
        ArrayList<Double> sparsity = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : candidates) {
            sparsity.add((double) candidate.size());
        }
        return sparsity;
    }

    /**
     * determines the abnormality of the candidates, i.e. how abnormal the events in a candidate are compared to all events
     *
     * @param candidates the set the abnormality is to be determined for
     * @param logEntries the list of Home Assistant Logs
     * @return a percentage out of how many changes to a certain entityId the change was the same as the one
     * in the specific entry
     */
    public ArrayList<Double> calculateAbnormality
    (ArrayList<ArrayList<LogEntry>> candidates, ArrayList<LogEntry> logEntries) {

        ArrayList<Double> abnormality = new ArrayList<>();

        ArrayList<LogEntry> currentState = getCurrentState(logEntries);

        for (ArrayList<LogEntry> candidate : candidates) {
            double sum = 0.0;
            for (LogEntry c : candidate) {

                Double identical = 0.0;     // amount of logEntries which are identical to c (same state and entityId)
                Double all = 0.0;           // amount of logEntries that have the same entityId
                String entityId = c.getEntityId();
                String state = c.getState();
                for (LogEntry logEntry : logEntries) {

                    if (entityId.equals(logEntry.getEntityId())) {
                        all++;
                        if (state.equals(logEntry.getState())) {
                            identical++;
                        }
                    }

                }
                if (currentState.contains(c)) { // c is part of a subtractive explanation --> abnormality is beneficial
                    sum += identical / all;
                } else {    // c is part of an additive explanation --> abnormality is not beneficial
                    sum += 1 - identical / all; // = abnormality(all other states of the device except for c)
                }


            }

            Double average = sum / candidate.size();

            abnormality.add(average);
        }

        return abnormality;
    }

    /**
     * determines the temporality of the candidates, i.e. the average of how long-ago each entry was last true in seconds
     *
     * @param candidates  the set the temporality is to be determined for
     * @param explanandum the situation to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return for each candidate the average of how long-ago each entry was last true in seconds
     */
    public ArrayList<Double> calculateTemporality(ArrayList<ArrayList<LogEntry>> candidates, LogEntry
            explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<Double> temporality = new ArrayList<>();

        for (ArrayList<LogEntry> candidate : candidates) {
            double sum = 0.0;

            for (LogEntry c : candidate) {

                LogEntry newest = c;
                for (LogEntry logEntry : logEntries) {

                    int timeComparisonExp_1 = newest.compareTo(explanandum);
                    int timeComparisonExp_2 = logEntry.compareTo(explanandum);
                    // logEntry is identical to c, occurred before explanandum and newest occurred after explanandum
                    if (newest.equals(logEntry) && timeComparisonExp_1 > 0 && timeComparisonExp_2 < 0) {
                        newest = logEntry;
                    }

                    int timeComparisonLog = newest.compareTo(logEntry);
                    //logEntry is identical to c but occurred later and logEntry occurred before explanandum
                    if (newest.equals(logEntry) && timeComparisonLog < 0 && timeComparisonExp_2 < 0) {
                        newest = logEntry;
                    }
                }

                if (newest.compareTo(explanandum) > 0) {  //no identical logEntry before explanandum found
                    LOGGER.info("For LogEntry " + newest.getEntityId() + " with state " + newest.getState() + " no entry before the explanandum could be found. The temporality has been set to max.");
                    sum = Integer.MAX_VALUE;
                    break;
                }

                LocalDateTime current = explanandum.getLocalDateTime();
                LocalDateTime last = newest.getLocalDateTime();
                sum += (double) last.until(current, SECONDS);
            }

            Double average = sum / candidate.size();
            temporality.add(average);
        }
        return temporality;
    }

    /**
     * determines the proximity of the candidates, i.e. the amount of changes to the system if
     * a candidate was manipulated to be true. This takes into consideration which rules would fire when the changes are done
     *
     * @param candidates  the set the proximity is to be determined for
     * @param explanandum the situation to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return how many changes there would be in the system if all entries in a candidate are included
     * in the current state
     */
    public ArrayList<Double> calculateProximity(ArrayList<ArrayList<LogEntry>> candidates, LogEntry
            explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<Double> proximity = new ArrayList<>();

        // create deep copy of candidates
        ArrayList<ArrayList<LogEntry>> candidatesCopy = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : candidates) {
            candidatesCopy.add(new ArrayList<>(candidate));
        }

        for (ArrayList<LogEntry> candidate : candidatesCopy) {
            ArrayList<Rule> rules = new ArrayList<>(dataSer.findAllRules());

            // state of the system before the manipulations of this iteration are included
            ArrayList<LogEntry> currentState = new ArrayList<>(getCurrentState(logEntries));

            Iterator<LogEntry> i = candidate.iterator();
            ArrayList<LogEntry> toAdd = new ArrayList<>();
            ArrayList<LogEntry> toRemove = new ArrayList<>();
            while (i.hasNext()) {
                LogEntry c = i.next();
                // c is part of a subtractive explanation, proximity calculates changes if c was removed --> consider state before c
                if (currentState.contains(c)) {
                    if (c.getTime() == null) {  // find LogEntry that contains the time
                        for (LogEntry entry : currentState) {
                            if (entry.equals(c)) {
                                c.setTime(entry.getTime());
                            }
                        }
                    }
                    toAdd.add(getPreviousLogEntry(c, logEntries));
                    toRemove.add(c);
                }
            }
            candidate.addAll(toAdd);
            candidate.removeAll(toRemove);

            // all changes that are added in this iteration
            ArrayList<LogEntry> changesIteration = new ArrayList<>(candidate);

            // all changes over all iterations
            ArrayList<LogEntry> allChanges = new ArrayList<>(candidate);

            boolean somethingChanged = true;
            while (somethingChanged) {

                // all active rules at the state of stateBefore
                ArrayList<Rule> activeRules = new ArrayList<>(rules);
                TruePreconditions(activeRules, explanandum, currentState, logEntries);
                activeRules = removeOverridden(activeRules, logEntries);

                // collect all changes to the system from the active rules in the changesIteration array
                for (Rule r : activeRules) {
                    changesIteration.addAll(r.getActions());
                }
                noDuplicates(changesIteration);

                // remove entry if it is already in stateBefore
                changesIteration.removeIf(currentState::contains);

                allChanges.addAll(changesIteration);

                // update current state
                for (LogEntry changeIteration : changesIteration) {
                    Iterator<LogEntry> j = currentState.iterator();
                    ArrayList<LogEntry> changeToAdd = new ArrayList<>();
                    while (j.hasNext()) {
                        LogEntry current = j.next();
                        if (current.getEntityId().equals(changeIteration.getEntityId())) {
                            //remove current, add action to changes between manipulated and current state
                            j.remove();
                            changeToAdd.add(changeIteration);
                        }
                    }
                    currentState.addAll(changeToAdd);
                }

                somethingChanged = !changesIteration.isEmpty();
                changesIteration.clear();
            }

            noDuplicates(allChanges);
            proximity.add((double) allChanges.size());
        }
        return proximity;
    }

    // Auxiliary Methods (move to different class?):

    /**
     * Find the logEntry that changed the device to the state it had before the explanandum
     *
     * @param explanandum the situation to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return the logEntry that changed the device to the state it had before the explanandum LogEntry
     */
    public LogEntry getPreviousLogEntry(LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        String entityID = explanandum.getEntityId();
        String state = explanandum.getState();
        ArrayList<LogEntry> logEntriesSorted = new ArrayList<>(logEntries);
        logEntriesSorted.sort(Collections.reverseOrder());
        logEntriesSorted.remove(explanandum);

        for (LogEntry logEntry : logEntriesSorted) {
            int timeComparison = explanandum.compareTo(logEntry);
            if (timeComparison > 0 && entityID.equals(logEntry.getEntityId()) && !state.equals(logEntry.getState())) {
                return logEntry;
            }
        }
        // no previous state found
        return explanandum;
    }

    /**
     * determines the current state of the system
     *
     * @param logEntries the list of Home Assistant logs
     * @return a list of logEntries which contains for each entityID the newest state, i.e. the current state of this entityID
     */
    public ArrayList<LogEntry> getCurrentState(ArrayList<LogEntry> logEntries) {

        //logEntries are already given in reverse order, i.e. from newest to oldest
        //add logEntry to new List if this entityID has not occurred before
        ArrayList<LogEntry> logEntriesSorted = new ArrayList<>(logEntries);
        logEntriesSorted.sort(Collections.reverseOrder());
        ArrayList<LogEntry> currentState = new ArrayList<>();
        ArrayList<String> notYetConsideredIDs = new ArrayList<>();
        for (LogEntry entry : logEntriesSorted) {
            String id = entry.getEntityId();
            if (!notYetConsideredIDs.contains(id)) {
                notYetConsideredIDs.add(id);
            }
        }
        for (LogEntry entry : logEntriesSorted) {
            String id = entry.getEntityId();
            if (notYetConsideredIDs.contains(id)) {
                currentState.add(entry);
                notYetConsideredIDs.remove(id);
            }
        }
        return currentState;
    }

    /**
     * Checks if a rule has true preconditions
     *
     * @param r           rule for which we want to check if the preconditions are true
     * @param explanandum the situation to be explained
     * @param logEntries  list of the Home Assistant logs
     * @return if the rule has true preconditions
     */
    public boolean hasTruePreconditions(Rule r, LogEntry
            explanandum, ArrayList<LogEntry> currentState, ArrayList<LogEntry> logEntries) {
        boolean truePreconditions = true;
        if (explanandum.getTime() != null) { //suppose it has true trigger if it cannot be computed
            truePreconditions = findCauseSer.preconditionsApply(explanandum, r, logEntries);
        }

        ArrayList<LogEntry> conditions = r.getConditions();
        for (LogEntry condition : conditions) {
            if (condition != null && condition.getEntityId() != null && !currentState.contains(condition)) {
                truePreconditions = false;
                break;
            }
        }
        return truePreconditions;
    }

    /**
     * removes all rules with false preconditions
     *
     * @param rules       rules for which we want to check if they have true preconditions
     * @param explanandum the situation to be explained
     * @param logEntries  the list of Home Assistant logs
     */
    public void TruePreconditions(ArrayList<Rule> rules, LogEntry
            explanandum, ArrayList<LogEntry> currentState, ArrayList<LogEntry> logEntries) {
        if (rules != null && !rules.isEmpty()) {
            rules.removeIf(rule -> !hasTruePreconditions(rule, explanandum, currentState, logEntries));
        }
    }


    /**
     * Returns the controllability of the entityId.
     * If the entityId does not have a corresponding entity, "actionable" is returned.
     *
     * @param entityId entityId of a LogEntry
     * @return controllability of the entity associated to the entityId
     */
    public String getControllabilityByEntityId(String entityId) {
        Entity entity = dataSer.findEntityByEntityID(entityId);
        if (entity == null) {
            LOGGER.info("No corresponding entity to entityId " + entityId + " could be found. The controllability has been set to actionable.");
            return "actionable";
        }
        return entity.getControllability();
    }


    /**
     * removes the duplicates from the list
     *
     * @param list the list the duplicates are to be removed from
     * @param <T>  any
     */
    public <T> void noDuplicates(ArrayList<T> list) {
        Set<T> set = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(set);
    }

    /**
     * removes all rules from the input that are overridden
     *
     * @param rules      the rules with true preconditions
     * @param logEntries the list of Home Assistant logs
     * @return the subset of rules that are not being overridden by other rules in rules
     */
    public ArrayList<Rule> removeOverridden(ArrayList<Rule> rules, ArrayList<LogEntry> logEntries) {
        ArrayList<Rule> modifiedRules = new ArrayList<>(rules);
        for (LogEntry logEntry : logEntries) {
            String entityIdEntry = logEntry.getEntityId();
            ArrayList<Rule> sameAction = new ArrayList<>(); // action has the same entityId
            for (Rule r : rules) {
                ArrayList<LogEntry> actions = r.getActions();
                for (LogEntry action : actions) {
                    if (action.getEntityId().equals(entityIdEntry)) {
                        sameAction.add(r);
                    }
                }
            }
            if (!sameAction.isEmpty()) {
                Rule maxPriorityRule = Collections.max(sameAction, Comparator.comparingInt(Rule::getPriority));
                sameAction.remove(maxPriorityRule);
                for (Rule r : modifiedRules) {
                    if (sameAction.contains(r)) {
                        ArrayList<LogEntry> actions = r.getActions();
                        actions.remove(logEntry);
                        r.setActions(actions);
                    }
                }
            }
        }
        return modifiedRules;

    }


    /**
     * determines all permutations of lists
     *
     * @param lists   the list to be permuted
     * @param result  the result
     * @param depth   the depth of the permutation
     * @param current the current result
     */
    void generatePermutations(ArrayList<ArrayList<LogEntry>> lists, ArrayList<ArrayList<LogEntry>> result,
                              int depth, ArrayList<LogEntry> current) {
        if (depth == lists.size()) {
            result.add(current);
            return;
        }
        for (int i = 0; i < lists.get(depth).size(); i++) {
            current.add(lists.get(depth).get(i));
            generatePermutations(lists, result, depth + 1, current);
        }

    }


}
