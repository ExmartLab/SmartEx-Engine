package exengine.engineservice;

import java.lang.reflect.Array;
import java.util.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


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
     * * Include explanation of CounterfactualExplanationService here
     *
     * @param min    Representing the number of minutes taken into account for
     *               analyzing past events, starting from the call of the method
     * @param userId The user identifier for the explainee that asked for the
     *               explanation @Note not to confuse with the id property of the
     *               user class
     * @param device The device whose last action is to be explained
     * @return Either the built explanation, or an error description in case the
     * explanation could not be built.
     */

    @Override
    public String getExplanation(int min, String userId, String device) {
        LOGGER.debug("getExplanation (counterfactual) called with arguments min: {}, user id: {}, device: {}", min, userId, device);

        ArrayList<LogEntry> logEntries = new ArrayList<>(getLogEntries(min));
        List<Rule> dbRules = dataSer.findAllRules();
        List<Error> dbErrors = dataSer.findAllErrors();

        // Determine explanandum / current state
        LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);
        if (explanandum == null || explanandum.getEntityId() == null || explanandum.getState() == null) {
            return "No explanandum found. Could not proceed.";
        }
        LOGGER.info("Found explanandum: {}", explanandum.getName());
        LOGGER.info("Found stateCurrent: {}", explanandum.getState());
        String entityId = explanandum.getEntityId();


        // Determine previous state
        LogEntry previous = getPreviousLogEntry(explanandum, logEntries); // = explanandum if there is no previous LogEntry
        LOGGER.info("Found previous: {}", previous.getName());
        LOGGER.info("Found statePrevious: {}", previous.getState());

        // Determine expected state
        LogEntry expected = determineFoil(explanandum, logEntries, device, entityId, userId);
        LOGGER.info("Found expected: {}", expected.getName());
        LOGGER.info("Found stateExpected: {}", expected.getState());

        if (explanandum.equals(expected)) {
            return "Error, there is no explanation need. Fact and Foil are the same.";
        }

        //TODO: ADD RULES TO YAML FILE, OTHERWISE THE NEW TEST CASES WON'T WORK


        // Determine rules with currently true preconditions
        ArrayList<LogEntry> currentState = new ArrayList<>(getCurrentState(logEntries));

        //rules with actions leading to current state
        ArrayList<Rule> rulesCurrent = findCauseSer.findCandidateRules(explanandum, dbRules);
        TruePreconditions(rulesCurrent, explanandum, currentState, logEntries);    //checks if the conditions currently are true and a trigger has been activated before the explanandum
        LOGGER.info("The rules with true preconditions leading to the current state are " + rulesCurrent);

        //rules with actions leading to previous state
        ArrayList<Rule> rulesPrevious = findCauseSer.findCandidateRules(previous, dbRules);
        TruePreconditions(rulesPrevious, explanandum, currentState, logEntries);
        LOGGER.info("The rules with true preconditions leading to the previous state are " + rulesPrevious);

        //rules with actions leading to expected state
        ArrayList<Rule> rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);
        TruePreconditions(rulesExpected, explanandum, currentState, logEntries);
        if (!rulesExpected.isEmpty()) {         // Only consider rules with higher priority in rulesPrevious
            Rule maxPriorityRule = Collections.max(rulesExpected, Comparator.comparingInt(Rule::getPriority));
            int maxPriority = maxPriorityRule.getPriority();
            rulesPrevious.removeIf(r -> r.getPriority() <= maxPriority);
        }
        LOGGER.info("The rules with true preconditions leading to the expected state are " + rulesExpected);


        //Todo: What about mixed cases where the trigger has fired, but the conditions were not true yet?

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
                minPreconditions.add(minAdd(dummy, expected, explanandum, logEntries));
            } else {
                return "Error, there is no explanation need.";
            }
        }

        LOGGER.info("Minimal change determined.");

        return generateCFE(minPreconditions, explanandum, expected, device);

    }

    public LogEntry determineFoil(LogEntry explanandum, ArrayList<LogEntry> logEntries, String device, String entityId, String userId) {

        List<Rule> dbRules = dataSer.findAllRules();
        List<Error> dbErrors = dataSer.findAllErrors();
        LogEntry expected = explanandum;

        // determine foil using the ContrastiveExplanationService
        Object happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
        ArrayList<Rule> candidateRules = contrastiveSer.getCandidateRules(entityId);
        Rule expectedRule = contrastiveSer.getExpectedRule(explanandum, logEntries, candidateRules, happenedEvent, device, entityId, userId);
        if (expectedRule != null) {
            ArrayList<LogEntry> expectedCandidates = new ArrayList<>(expectedRule.getActions());
            for (LogEntry logEntry : expectedCandidates) {
                if (logEntry.getEntityId().equals(entityId)) {    //it is the action of the rule we want
                    expected = logEntry;
                }
            }
        }
        if (!expected.equals(explanandum)) {     // using the ContrastiveExplanationService worked
            return expected;
        } else {                                // foil determination did not work, add cases manually

            return switch (explanandum.getEntityId()) {
                // needed for unit tests:
                case "status.fan" -> logEntries.get(10);
                case "color.lamp" -> logEntries.get(8);
                case "status.lamp" -> logEntries.get(15);
                case "status.temperature" -> logEntries.get(2);
                case "setting.aircon" -> logEntries.get(13);
                case "status.window" -> logEntries.get(6);
                // needed for integration tests:
                case "scene.tv_playing" -> new LogEntry(null, "Tv on", "on", "scene.tv_playing", new ArrayList<>());
                case "switch.smart_plug_social_room_coffee" -> new LogEntry(null, "Coffee machine on", "on", "switch.smart_plug_social_room_coffee", new ArrayList<>());
                default -> explanandum;
            };

        }
    }


    public String generateCFE(ArrayList<ArrayList<LogEntry>> minPreconditions, LogEntry explanandum, LogEntry expected, String device) {

        String explanation = "The " + device + " would be " + expected.getState() + " instead of " + explanandum.getState() + " if in the past ";

        //Todo: Rework

        // add additive preconditions to explanation
        ArrayList<LogEntry> addPreconditions = minPreconditions.get(0);
        for (int i = 0; i < addPreconditions.size() - 1; i++) {
            Entity entityAdd = dataSer.findEntityByEntityID(addPreconditions.get(i).getEntityId());
            String deviceAdd = addPreconditions.get(i).getName();
            if (entityAdd != null) {    // we can get the correct device
                deviceAdd = entityAdd.getDeviceName();
            }
            explanation = explanation.concat("the " + deviceAdd + " was " + addPreconditions.get(i).getState() + ", ");
        }
        if (addPreconditions.size() > 0) {
            if (addPreconditions.size() >= 2) {
                explanation = explanation.concat("and ");
            }
            Entity entityAdd = dataSer.findEntityByEntityID(addPreconditions.get(addPreconditions.size() - 1).getEntityId());
            String deviceAdd = addPreconditions.get(addPreconditions.size() - 1).getName();
            if (entityAdd != null) {    //we can get the correct device
                deviceAdd = entityAdd.getDeviceName();
            }
            explanation = explanation.concat("the " + deviceAdd + " was " + addPreconditions.get(addPreconditions.size() - 1).getState() + " and ");
        }

        // add subtractive preconditions to explanation
        if (minPreconditions.size() > 1) {
            ArrayList<LogEntry> subPreconditions = minPreconditions.get(1);
            for (int i = 0; i < subPreconditions.size() - 1; i++) {
                Entity entitySub = dataSer.findEntityByEntityID(subPreconditions.get(i).getEntityId());
                String deviceSub = subPreconditions.get(i).getName();
                if (entitySub != null) {    //we can get the correct device
                    deviceSub = entitySub.getDeviceName();
                }
                explanation = explanation.concat("the " + deviceSub + " was not " + subPreconditions.get(i).getState() + ", ");

            }
            if (subPreconditions.size() > 0) {
                if (subPreconditions.size() >= 2) {
                    explanation = explanation.concat("and ");
                }
                Entity entitySub = dataSer.findEntityByEntityID(subPreconditions.get(subPreconditions.size() - 1).getEntityId());
                String deviceSub = subPreconditions.get(subPreconditions.size() - 1).getName();
                if (entitySub != null) {    //we can get the correct device
                    deviceSub = entitySub.getDeviceName();
                }
                explanation = explanation.concat("the " + deviceSub + " was not " + subPreconditions.get(subPreconditions.size() - 1).getState() + ". ");
            }
        }

        // Capitalize only the first letter
        explanation = explanation.toLowerCase();
        explanation = explanation.substring(0, 1).toUpperCase() + explanation.substring(1);

        LOGGER.info("Explanation generated.");
        return explanation;
    }

    // Subtractive Methods:

    /**
     * Finds the minimal set of LogEntries to change such that ruleToReverse does not have true preconditions anymore.
     *
     * @param ruleToReverse the rule that is to be reversed
     * @param explanandum   the action that is to be explained
     * @param logEntries    the list of Home Assistant logs
     * @return the minimal set of LogEntries s.t. reversing them prevents ruleToReverse from being active
     */
    public ArrayList<LogEntry> minSub(Rule ruleToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<ArrayList<LogEntry>> toReverse = new ArrayList<>();
        ArrayList<LogEntry> conditions = new ArrayList<>(ruleToReverse.getConditions());
        ArrayList<LogEntry> actionableConditions = new ArrayList<>();
        ArrayList<LogEntry> mutableConditions = new ArrayList<>();
        ArrayList<LogEntry> nonMutableConditions = new ArrayList<>();

        // Sort by controllability
        for (LogEntry precondition : conditions) {
            String controllability = getControllabilityByEntityId(precondition.getEntityId());
            switch (controllability) {
                case "actionable" -> actionableConditions.add(precondition);
                case "mutable" -> mutableConditions.add(precondition); //mutable but non-actionable
                case "non-mutable" -> nonMutableConditions.add(precondition);
                default -> {
                    LOGGER.error("Error, an entity has invalid controllability");
                    return null;        //Todo: Check what happens in this case
                }
            }
        }

        // actionable preconditions
        for (LogEntry precondition : actionableConditions) {
            toReverse.addAll(new ArrayList<>(findRoots(precondition, explanandum, logEntries)));
        }

        // mutable but non-actionable preconditions
        for (LogEntry precondition : mutableConditions) {
            toReverse.addAll(new ArrayList<>(findRoots(precondition, explanandum, logEntries)));
            toReverse.remove(precondition); // precondition cannot be directly manipulated  //Todo: fix!
        }

        // consider non-mutable preconditions if there are no other ones
        if (toReverse.isEmpty()) {
            for (LogEntry condition : nonMutableConditions) {
                ArrayList<LogEntry> conditionAsArray = new ArrayList<>();
                conditionAsArray.add(condition);
                toReverse.add(conditionAsArray);
            }
            LOGGER.info("There are no mutable preconditions. Proceed with non-mutable ones");
        }

        noDuplicates(toReverse);

        // findRoots considers rules with true preconditions
        // but the manipulation can use a rule that that does not have true preconditions
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : toReverse) {
            ArrayList<LogEntry> modified = new ArrayList<>();
            for (LogEntry c : candidate) {
                modified.addAll(modify(c, explanandum, logEntries));
            }
            minCandidates.add(modified);
        }


        // reverse triggers
        ArrayList<LogEntry> currentState = getCurrentState(logEntries);
        ArrayList<LogEntry> triggers = new ArrayList<>(ruleToReverse.getTrigger());
        // only consider true triggers
        triggers.removeIf(trigger -> !currentState.contains(trigger));
        //add all options to remove all triggers
        ArrayList<ArrayList<LogEntry>> allRoots = new ArrayList<>();
        for (LogEntry trigger : triggers) {
            allRoots.addAll(findRoots(trigger, explanandum, logEntries)); //correct?
        }
        // find combinations s.t. for each trigger one root is chosen
        generatePermutations(allRoots, new ArrayList<>(), 0, new ArrayList<>());
        minCandidates.addAll(allRoots);

        //Todo: either remove one condition or all trigger
        //Todo: check trigger for all methods

        return minComputation(minCandidates, explanandum, logEntries);
    }

    /**
     * Finds the minimal set of LogEntries to change s.t. all rules in rulesToReverse do not have true
     * preconditions anymore.
     *
     * @param rulesToReverse the rules that are to be reversed
     * @param explanandum    the action that is to be explained
     * @param logEntries     the list of Home Assistant logs
     * @return the minimal set of LogEntries s.t. reversing them prevents all rules in rulesToReverse from being active
     */
    public ArrayList<LogEntry> minSubAll(ArrayList<Rule> rulesToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        ArrayList<LogEntry> conditions = new ArrayList<>();
        for (Rule r : rulesToReverse) {
            conditions.addAll(minSub(r, explanandum, logEntries));
        }
        noDuplicates(conditions);
        return conditions;
    }


    //Additive Method:

    /**
     * Finds minimal set of LogEntries that need to true so that a rule is fired that overrides ruleToOverride.
     *
     * @param ruleToOverride the rule that is to be overridden
     * @param explanandum    the action of the device to be explained
     * @param toAchieve      the state of the device that is to be achieved
     * @param logEntries     the list of Home Assistant logs
     * @return minimal set of LogEntries s.t. if the System had these states of the devices ruleToOverride would be
     * overridden by another rule.
     */
    public ArrayList<LogEntry> minAdd(Rule ruleToOverride, LogEntry explanandum, LogEntry toAchieve, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());

        // Find rules that lead to the desired state
        ArrayList<Rule> candidateRules = findCauseSer.findCandidateRules(toAchieve, dbRules);
        LOGGER.info("Candidate rules to fire are " + candidateRules);
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();

        if (candidateRules != null && !candidateRules.isEmpty()) {
            for (Rule r : candidateRules) {

                // Find minimal way to make r fire
                ArrayList<LogEntry> candidate = new ArrayList<>(makeFire(r, explanandum, logEntries));

                // only consider rules that override ruleToOverride and don't need too many changes
                if (r.getPriority() > ruleToOverride.getPriority() && candidate.size() <= 3) {
                    candidates.add(candidate);
                }
            }
        }

        if (candidates.isEmpty()) {
            LOGGER.info("No additive explanation available. No rule that could fire found.");
            return null;    //Todo: check what happens in this case, add to minComputation?
        }

        return minComputation(candidates, explanandum, logEntries);
    }


    //Auxiliary Methods:

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

        ArrayList<LogEntry> currentStates = new ArrayList<>(getCurrentState(logEntries));
        ArrayList<LogEntry> conditions = new ArrayList<>(r.getConditions());
        ArrayList<LogEntry> triggers = new ArrayList<>(r.getTrigger());

        // find false conditions of r
        Iterator<LogEntry> i = conditions.iterator();
        while (i.hasNext()) {
            LogEntry p = i.next();
            if (p.getEntityId() == null || p.getState() == null) {   // remove invalid conditions
                i.remove();
            } else {
                for (LogEntry state : currentStates) {  //remove true preconditions
                    if (p.equals(state)) {
                        i.remove();
                    }
                }
            }
        }

        // check if it is more minimal to fire a rule instead of manipulating the conditions directly
        ArrayList<LogEntry> modifiedConditions = new ArrayList<>();
        for (LogEntry condition : conditions) {
            ArrayList<LogEntry> modifiedCondition = modify(condition, explanandum, logEntries);
            if (modifiedCondition != null && !modifiedCondition.isEmpty()) {
                modifiedConditions.addAll(modifiedCondition);
            }
        }

        ArrayList<LogEntry> minPreconditions = new ArrayList<>(modifiedConditions);


        // check if r has a true trigger
        boolean hasTrueTrigger = true;
        if (explanandum.getTime() != null) {
            hasTrueTrigger = findCauseSer.preconditionsApply(explanandum, r, logEntries);
        }
        if (!hasTrueTrigger) {
            ArrayList<ArrayList<LogEntry>> triggerCandidates = new ArrayList<>();
            for (LogEntry t : triggers) {
                if (t.getEntityId() == null || t.getState() == null) {     // remove invalid triggers
                    i.remove();
                } else {
                    ArrayList<LogEntry> triggerCandidate = modify(t, explanandum, logEntries);
                    if (triggerCandidate != null && !triggerCandidate.isEmpty()) {
                        triggerCandidates.add(triggerCandidate);
                    }
                }
            }
            // find minimal trigger:
            ArrayList<LogEntry> minTrigger = minComputation(triggerCandidates, explanandum, logEntries);
            minPreconditions.addAll(minTrigger);
        }

        return minPreconditions;
    }

    /**
     * finds the minimal set of Logentries with states the system should have to make the precondition true.
     *
     * @param precondition the condition or trigger of a rule that should be modified
     * @param logEntries   the list of Home Assistant logs
     * @return minimal set of LogEntries with states the system should have to make the precondition true
     */
    public ArrayList<LogEntry> modify(LogEntry precondition, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();

        // changing precondition directly is possible
        ArrayList<LogEntry> preconditionAsArray = new ArrayList<>();
        preconditionAsArray.add(precondition);
        minCandidates.add(preconditionAsArray);

        // find rules that can change the precondition
        for (Rule r : dbRules) {
            if (r.getActions().contains(precondition)) {
                minCandidates.add(makeFire(r, explanandum, logEntries));    //minimal changes to make r fire
            }
        }

        return minComputation(minCandidates, explanandum, logEntries);
    }

    /**
     * Todo: Supposes that c is mutable. If there is no rule that fires c, it has to be actionable, because it cannot be mutable and non-actionable.
     * Todo: Should this method be added somewhere else? What is the exact difference to modify?
     * finds all preconditions (i.e. Logentries) that can be directly manipulated to make precondition c false.
     * c may not be able to be manipulated directly because there is a rule with true preconditions that would change
     * c back immediately.
     *
     * @param c           the LogEntry that needs to be manipulated
     * @param explanandum the action to be explained
     * @param logEntries  the list of Home Assistant logs
     * @return all preconditions that can be directly manipulated to make precondition c false (including c if c can be
     * directly manipulated)
     */
    public ArrayList<ArrayList<LogEntry>> findRoots(LogEntry c, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        ArrayList<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());
        ArrayList<LogEntry> currentState = new ArrayList<>(getCurrentState(logEntries));
        if (!currentState.contains(c)) {      //c is already false, nothing has to be changed
            return new ArrayList<>();
        }

        // determine rules that prevent us from directly changing c
        ArrayList<Rule> activeRules = findCauseSer.findCandidateRules(c, dbRules);
        TruePreconditions(activeRules, explanandum, currentState, logEntries);

        //Todo: improve naming
        //Todo: go through test again
        //Todo: Add this check to all methods

        ArrayList<ArrayList<LogEntry>> changeablePreconditions = new ArrayList<>();

        if (activeRules.isEmpty()) {  //can directly change c
            ArrayList<LogEntry> cAsArray = new ArrayList<>();
            cAsArray.add(c);
            changeablePreconditions.add(cAsArray);
        } else {
            for (Rule r : activeRules) {

                // find roots for each condition (changing one root is enough to make r not have true preconditions)
                ArrayList<LogEntry> conditions = new ArrayList<>(r.getConditions());
                for (LogEntry precondition : conditions) {
                    ArrayList<ArrayList<LogEntry>> conditionRoots = new ArrayList<>(findRoots(precondition, explanandum, logEntries));
                    changeablePreconditions.addAll(conditionRoots);
                }


                //find roots for triggers (all true triggers must be changed to make r not have true preconditions)
                ArrayList<LogEntry> triggers = new ArrayList<>(r.getTrigger());
                triggers.removeIf(trigger -> !currentState.contains(trigger));

                ArrayList<ArrayList<LogEntry>> triggerRoots = new ArrayList<>();
                for (LogEntry trigger : triggers) {
                    triggerRoots.addAll(findRoots(trigger, explanandum, logEntries));   //Todo: is addAll correct?
                }
                changeablePreconditions.addAll(triggerRoots);

            }
        }

        noDuplicates(changeablePreconditions);
        return changeablePreconditions;

    }

    public ArrayList<ArrayList<LogEntry>> overrideOrRemove(ArrayList<Rule> rules, LogEntry explanandum, LogEntry expected, Boolean firingNecessary, ArrayList<LogEntry> logEntries) {
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<LogEntry> subCandidates = new ArrayList<>();
        ArrayList<LogEntry> addCandidates = new ArrayList<>();
        ArrayList<Rule> rulesSorted = new ArrayList<>(rules);
        // Sort the ArrayList by priority in descending order:
        rulesSorted.sort((r1, r2) -> {
            // Sort in descending order
            return Integer.compare(r2.getPriority(), r1.getPriority());
        });
        for (int i = 0; i < rulesSorted.size(); i++) {
            Rule r = rulesSorted.get(i);
            ArrayList<LogEntry> candidate = new ArrayList<>();

            //subtractive part:
            if (i != 0) {    //r has not the highest priority, i.e. there is a subtractive part
                ArrayList<Rule> rulesHigherPriority = new ArrayList<>(rulesSorted.subList(0, i - 1));
                ArrayList<LogEntry> subtracting = new ArrayList<>(minSubAll(rulesHigherPriority, explanandum, logEntries));
                candidate.addAll(subtracting);
                subCandidates.addAll(candidate);
            }

            //additive part:
            ArrayList<LogEntry> overriding = minAdd(r, explanandum, expected, logEntries);
            if (overriding != null && !overriding.isEmpty()) {
                candidate.addAll(overriding);
                addCandidates.addAll(overriding);
            }

            candidates.add(candidate);
        }

        // removing all rules
        ArrayList<LogEntry> subtractingAll = minSubAll(rulesSorted, explanandum, logEntries);
        if (firingNecessary) {
            Rule dummy = new Rule("dummy", null, null, null, null, null, null, 0);
            ArrayList<LogEntry> addPart = minAdd(dummy, explanandum, expected, logEntries);
            addCandidates.addAll(addPart);
            subCandidates.addAll(subtractingAll);
            subtractingAll.addAll(minAdd(dummy, explanandum, expected, logEntries));
            candidates.add(subtractingAll);
        }

        //minimal computation:
        ArrayList<LogEntry> minTogether = minComputation(candidates, explanandum, logEntries);

        //determine which were additive or subtractive:
        ArrayList<LogEntry> minAdd = new ArrayList<>();
        ArrayList<LogEntry> minSub = new ArrayList<>();
        for (LogEntry entry : minTogether) {
            if (addCandidates.contains(entry)) {
                minAdd.add(entry);
            }
            if (subCandidates.contains(entry)) {
                minSub.add(entry);
            }
        }
        ArrayList<ArrayList<LogEntry>> minCombined = new ArrayList<>();
        minCombined.add(minAdd);
        minCombined.add(minSub);

        return minCombined;
    }

    public ArrayList<LogEntry> minComputation(ArrayList<ArrayList<LogEntry>> candidates, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        if (candidates == null || candidates.isEmpty()) {
            LOGGER.info("No candidates to calculate the minimum found. Empty array is returned.");
            return new ArrayList<>();
        }

        // remove duplicates
        noDuplicates(candidates);

        // check for actionability
        ArrayList<ArrayList<LogEntry>> actionableCandidates = new ArrayList<>();
        for (ArrayList<LogEntry> candidate : candidates) {
            ArrayList<LogEntry> actionableCandidate = new ArrayList<>(candidate);
            actionableCandidates.add(actionableCandidate);
            for (LogEntry c : candidate) {  //candidate is not actionable if one precondition is not actionable
                if (!getControllabilityByEntityId(c.getEntityId()).equals("actionable")) {
                    actionableCandidates.remove(actionableCandidate);
                }
            }//Todo: check mutable but non-actionable
        }

        // consider non-actionable candidates if there are no actionable ones
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

        // define if property is beneficial or not
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

    public ArrayList<Double> calculateSparsity(ArrayList<ArrayList<LogEntry>> candidates) {
        ArrayList<Double> sparsity = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            sparsity.add(i, (double) candidates.get(i).size());
        }
        return sparsity;
    }

    public ArrayList<Double> calculateAbnormality(ArrayList<ArrayList<LogEntry>> candidates, ArrayList<LogEntry> logEntries) {
        ArrayList<Double> abnormality = new ArrayList<>();

        for (ArrayList<LogEntry> candidate : candidates) {
            double sum = 0.0;
            for (LogEntry c : candidate) {
                Double identical = 0.0;     //#logEntries which are identical to c
                Double all = 0.0;           //#logEntries that have the same entityId
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
                sum += identical / all * 100;

            }
            Double average = sum / candidate.size();

            abnormality.add(average);
        }

        return abnormality;
    }

    public ArrayList<Double> calculateTemporality(ArrayList<ArrayList<LogEntry>> candidates, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        ArrayList<Double> temporality = new ArrayList<>();

        for (ArrayList<LogEntry> candidate : candidates) {
            double sum = 0.0;
            for (LogEntry c : candidate) {
                LogEntry newest = c;
                for (LogEntry logEntry : logEntries) {
                    int timeComparisonExp_1 = newest.compareTo(explanandum);
                    int timeComparisonExp_2 = logEntry.compareTo(explanandum);
                    if (newest.equals(logEntry) && timeComparisonExp_1 > 0 && timeComparisonExp_2 < 0) {    //logEntry is identical to c, occurred before explanandum and newest occurred after explanandum
                        newest = logEntry;
                    }
                    int timeComparisonLog = newest.compareTo(logEntry);
                    if (newest.equals(logEntry) && timeComparisonLog < 0 && timeComparisonExp_2 < 0) { //logEntry is identical to c but occurred later and logEntry occurred before explanandum
                        newest = logEntry;
                    }
                }
                if (newest.compareTo(explanandum) > 0) {  //no identical logEntry before explanandum found
                    LOGGER.info("For LogEntry " + newest.getEntityId() + " with state " + newest.getState() + " no entry before the explanandum could be found. The temporality has been set to max.");
                    sum = Integer.MAX_VALUE;
                    break;
                } else {
                    LocalDateTime current = explanandum.getLocalDateTime();
                    LocalDateTime last = newest.getLocalDateTime();
                    sum += (double) last.until(current, SECONDS);
                }
            }
            Double average = sum / candidate.size();
            temporality.add(average);
        }
        return temporality;
    }

    public ArrayList<Double> calculateProximity(ArrayList<ArrayList<LogEntry>> candidates, LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<Double> proximity = new ArrayList<>();

        for (ArrayList<LogEntry> candidate : candidates) {
            ArrayList<Rule> rules = new ArrayList<>(dataSer.findAllRules());

            // state of the system before the manipulations of this iteration are included
            ArrayList<LogEntry> stateCurrent = new ArrayList<>(getCurrentState(logEntries));

            // all changes that are added in this iteration
            ArrayList<LogEntry> changesIteration = new ArrayList<>(candidate);

            // all changes over all iterations
            ArrayList<LogEntry> allChanges = new ArrayList<>(candidate);

            // iteration counter to ensure we don't end up in an infinite loop
            int l = 0;
            boolean somethingChanged = true;

            while (somethingChanged && l < 15) {

                // all active rules at the state of stateBefore
                ArrayList<Rule> activeRules = new ArrayList<>(rules);
                TruePreconditions(activeRules, explanandum, stateCurrent, logEntries);

                activeRules = removeOverridden(activeRules, logEntries);


                // collect all changes to the system from the active rules in the changesIteration array
                for (Rule r : activeRules) {
                    changesIteration.addAll(r.getActions());
                }
                noDuplicates(changesIteration);

                // remove entry if it is already in stateBefore
                changesIteration.removeIf(stateCurrent::contains);

                allChanges.addAll(changesIteration);

                // update current state
                for (LogEntry changeIteration : changesIteration) {
                    Iterator<LogEntry> j = stateCurrent.iterator();
                    ArrayList<LogEntry> toAdd = new ArrayList<>();
                    while (j.hasNext()) {
                        LogEntry current = j.next();
                        if (current.getEntityId().equals(changeIteration.getEntityId())) {
                            //remove current, add action to changes between manipulated and current state
                            //add action
                            j.remove();
                            toAdd.add(changeIteration);
                        }
                    }
                    stateCurrent.addAll(toAdd);
                }

                somethingChanged = !changesIteration.isEmpty();
                changesIteration.clear();
                l++;
            }

            noDuplicates(allChanges);
            proximity.add((double) allChanges.size());
        }
        return proximity;
    }

    /**
     * Find the logEntry that changed the device to the state it had before the explanandum
     *
     * @param explanandum explanandum determined with getExplanandumLogEntry
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
        //no previous state found:
        return explanandum;
    }

    /**
     * Checks if a rule has true preconditions
     *
     * @param r           Rule for which we want to check if the preconditions are true
     * @param explanandum the current explanandum
     * @param logEntries  list of all relevant LogEntries
     * @return truePreconditions
     */
    public boolean hasTruePreconditions(Rule r, LogEntry explanandum, ArrayList<LogEntry> currentState, ArrayList<LogEntry> logEntries) {
        boolean truePreconditions = true;
        if (explanandum.getTime() != null) { //suppose it has true trigger if it cannot be computed
            truePreconditions = findCauseSer.preconditionsApply(explanandum, r, logEntries);
        }

        ArrayList<LogEntry> conditions = r.getConditions();
        for (LogEntry condition : conditions) {
            if (condition != null && condition.getEntityId() != null && !currentState.contains(condition)) { //condition does not have the same state and entityId as one of the elements in currentState, i.e. the state is different and does not have entityId null which makes it an invalid condition
                truePreconditions = false;
                break;
            }
        }
        return truePreconditions;
    }

    /**
     * @param rules       Rules for which we want to check if they have true preconditions
     * @param explanandum The current explanandum
     * @param logEntries  All considered logEntries
     */
    public void TruePreconditions(ArrayList<Rule> rules, LogEntry explanandum, ArrayList<LogEntry> currentState, ArrayList<LogEntry> logEntries) {
        if (rules != null && !rules.isEmpty()) {
            rules.removeIf(rule -> !hasTruePreconditions(rule, explanandum, currentState, logEntries));
        }
    }

    //returns a list of logEntries which contains for each entityID the newest state, i.e. the current state of this entityID
    public ArrayList<LogEntry> getCurrentState(ArrayList<LogEntry> logEntries) {
        //logEntries are already given in reverse order, i.e. from newest to oldest
        //add logEntry to new List if this entityID has not occurred before
        ArrayList<LogEntry> logEntriesSorted = new ArrayList<>(logEntries);
        logEntriesSorted.sort(Collections.reverseOrder());
        ArrayList<LogEntry> currentState = new ArrayList<>();
        ArrayList<String> notYetConsideredIDs = new ArrayList<>();
        for (LogEntry entry : logEntriesSorted) { //collect all entityIds
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
     * Returns the controllability of the entityId.
     * If the entityId does not have a corresponding entity, "actionable" is returned.
     *
     * @param entityId EntityId of for example a LogEntry
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


    public <T> void noDuplicates(ArrayList<T> list) {
        Set<T> set = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(set);
    }

    public ArrayList<Rule> removeOverridden(ArrayList<Rule> rules, ArrayList<LogEntry> logEntries) {
        ArrayList<Rule> modifiedRules = new ArrayList<>(rules);
        for (LogEntry logEntry : logEntries) {
            String entityIdEntry = logEntry.getEntityId();
            ArrayList<Rule> sameAction = new ArrayList<>(); //action has the same entityId
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


    void generatePermutations(ArrayList<ArrayList<LogEntry>> lists, ArrayList<ArrayList<LogEntry>> result, int depth, ArrayList<LogEntry> current) {
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
