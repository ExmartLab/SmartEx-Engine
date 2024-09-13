package exengine.engineservice;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.explanationpresentation.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterfactualExplanationService extends ExplanationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterfactualExplanationService.class);

    @Autowired
    FindCauseService findCauseSer;

    @Autowired
    DatabaseService dataSer;

    @Autowired
    ContrastiveExplanationService contrastiveSer;

    /**
     * * Include explanation of CounterfactualExplanationService here
     * 
     * 
     * @param min    Representing the number of minutes taken into account for
     *               analyzing past events, starting from the call of the method
     * @param userId The user identifier for the explainee that asked for the
     *               explanation @Note not to confuse with the id property of the
     *               user class
     * @param device The device whose last action is to be explained
     * @return Either the built explanation, or an error description in case the
     *         explanation could not be built.
     */

    @Override
    public String getExplanation(int min, String userId, String device) {

        LOGGER.debug("getExplanation (counterfactual) called with arguments min: {}, user id: {}, device: {}", min, userId, device);

        String explanation = "found nothing to explain";

        ArrayList<LogEntry> logEntries = getLogEntries(min);
        LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);
        LogEntry previous = explanandum;
        LogEntry expected = explanandum;

        String entityId = "none";
        List<Rule> dbRules = dataSer.findAllRules();
        List<Error> dbErrors = dataSer.findAllErrors();
        Object happenedEvent = null;

        String stateCurrent;
        String statePrevious = null;

        if (explanandum == null) {  //the state has not changed in the last min minutes --> Fact = nothing
            LOGGER.info("No explanadum found. The fact is nothing");
        } else {
            LOGGER.info("Found explanandum: {}", explanandum);
            entityId = explanandum.getEntityId();

            /** determine current state: */
            stateCurrent = explanandum.getState();
            LOGGER.info("Found stateCurrent: {}", stateCurrent);

            /** determine previous state: */
            previous = getPreviousLogEntry(explanandum, logEntries);
            if (previous != null) {
                statePrevious = previous.getState();
            } else {
                statePrevious = stateCurrent;
            }
            LOGGER.info("Found previous: {}", previous);
            LOGGER.info("Found statePrevious: {}", statePrevious);

            //for expected state:
            happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
        }

        /** determine expected state: */
        ArrayList<Rule> candidateRules = contrastiveSer.getCandidateRules(entityId);
        Rule expectedRule = contrastiveSer.getExpectedRule(explanandum, logEntries, candidateRules,happenedEvent, device, entityId, userId);
        ArrayList<LogEntry> expectedCandidates = expectedRule.getActions();
        for (LogEntry logEntry : expectedCandidates) {
            if(logEntry.getEntityId().equals(entityId)){    //it is the action of the rule we want
                expected = logEntry;
            }
        }
        String stateExpected = expected.getState();    //action of the expectedRule from ContrastiveExplanationService


        ArrayList<Rule> rulesCurrent = new ArrayList<>();
        ArrayList<Rule> rulesPrevious = new ArrayList<>();
        ArrayList<Rule> rulesExpected = new ArrayList<>();

        if (explanandum != null) { // an event happened
            rulesCurrent = findCauseSer.findCandidateRules(explanandum, dbRules);  //rules with actions leading to state_current
            rulesPrevious = findCauseSer.findCandidateRules(previous, dbRules);  //rules with actions leading to state_current
            //Todo: have to have higher priority
            rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);  //rules with actions leading to state_current
            rulesCurrent = getRulesTruePreconditions(rulesCurrent, explanandum, logEntries);
            //Todo: Is previous correct here?
            rulesPrevious = getRulesTruePreconditions(rulesPrevious, previous, logEntries);
            rulesExpected = getRulesTruePreconditions(rulesExpected, expected, logEntries);
            ArrayList<Integer> priorityRulesExp = new ArrayList<>();
            for (Rule r: rulesExpected){
                priorityRulesExp.add(r.getPriority());
            }
            System.out.println(priorityRulesExp);



        }

        Boolean firingNecessary = true;
        ArrayList<LogEntry> minPreconditions = new ArrayList<>();
        if (!rulesCurrent.isEmpty()) {
            if (!rulesPrevious.isEmpty() || stateExpected == statePrevious){
               firingNecessary = false;
            }
                rulesCurrent.addAll(rulesPrevious);
            minPreconditions = overrideOrRemove(rulesCurrent, expected, firingNecessary, logEntries);
        } else {
            if (!rulesExpected.isEmpty()){
                minPreconditions = minAdd(null, expected, logEntries);
            } else {
                LOGGER.info("Error, there is no explanation need");
            }
        }

        LOGGER.info("Explanation generated");
        return generateCFE(minPreconditions);

    }


    public String generateCFE(Object min){
        return "counterfactual explanation";
    }

    /**Subtractive Methods:*/
    public ArrayList<LogEntry> minSub(Rule ruleToReverse, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> toReverse = new ArrayList<>();
        ArrayList<ArrayList<LogEntry>> toReverseArray = new ArrayList<>();
        ArrayList<LogEntry> preconditions = ruleToReverse.getConditions();
        preconditions.addAll(ruleToReverse.getTrigger());
        /** TODO: Implement mutability*/
        for (LogEntry precondition : preconditions){
            toReverse.addAll(findRoots(precondition,ruleToReverse));
        }
        for (LogEntry precondition : toReverse){
            ArrayList<LogEntry> updatedcsub = modify(precondition, logEntries);
            toReverseArray.add(updatedcsub);

        }
        return topsis(toReverseArray, logEntries);
    }

    public ArrayList<LogEntry> minSubAll(ArrayList<Rule> rulesToReverse, ArrayList<LogEntry> logEntries){
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> conditions = new ArrayList<>();
        for(Rule r : rulesToReverse){
            conditions.addAll(minSub(r ,logEntries));
        }
        return conditions;
    }


    /**Additive Method:*/
    public ArrayList<LogEntry> minAdd(Rule ruleToOverride, LogEntry expected, ArrayList<LogEntry>logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        if (ruleToOverride.getRuleId().isEmpty()){
            //TODO: Set Priority to 0, check that condition is correct
        }
        //TODO: add priority
        ArrayList<Rule> rulesAdd = findCauseSer.findCandidateRules(expected, dbRules);
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        for (Rule r : rulesAdd){
            ArrayList<LogEntry> candidate = makeFire(r, logEntries);
            if (candidate.size() > 3){
                rulesAdd.remove(r);
            }
        }
        if (rulesAdd.isEmpty()){
            LOGGER.info("No additive explanation available. No rule that could fire found.");
            return null;
        }
        return topsis(candidates, logEntries);
    }


    /**Auxilary Methods:*/

    public ArrayList<LogEntry> makeFire(Rule r, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> currentStates = getCurrentState(logEntries);
        ArrayList<LogEntry> preconditions = r.getConditions();
        ArrayList<LogEntry> triggers = r.getTrigger();
        for (LogEntry p : preconditions){
            for (LogEntry state : currentStates){
                if(p.equals(state)){  //check if the preconditions are already true
                    preconditions.remove(p);
                }
            }
        }
        for (LogEntry t : triggers){
            for (LogEntry state : currentStates){
                if(t.equals(state)){  //check if the preconditions are already true
                    triggers.remove(t);
                }
            }
        }
        ArrayList<LogEntry> updatedPreconditions = new ArrayList<>();
        ArrayList<LogEntry> updatedTriggers = new ArrayList<>();
        for (LogEntry c : preconditions){
            updatedPreconditions.addAll(modify(c, logEntries));
        }
        for (LogEntry c : triggers) {
            updatedTriggers.addAll(modify(c, logEntries));
        }
        //do not change r, just save the updated ones elsewhere, we do not want to change r forever in the system
        //r.setConditions(updatedPreconditions);
        //r.setTrigger(updatedTriggers);
        //todo: remove duplicates in all methods
        updatedPreconditions.addAll(updatedTriggers);
        return updatedPreconditions;
    }

    /**
     *
     * @param precondition      Condition or Trigger of a rule that contain the state the system should have
     *                          to make the rule fire.
     * @param logEntries        List of all logEntries that are considered
     * @return                  Minimal set of LogEntries with states that have to be changed to the mentioned state to make the logEntry precondition true
     */
    public ArrayList<LogEntry> modify(LogEntry precondition, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<Rule> rulesChangingPrecondition = new ArrayList<>();
        for (Rule r: dbRules){
            if ( r.getActions().contains(precondition)){   //there exists an action of r s.t. state and entityID of this action are the same as the ones of precondition,
                // i.e. firing this rule would lead to this state in this entityID
                rulesChangingPrecondition.add(r);
            }
        }
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();
        ArrayList<LogEntry> preconditionAsArray = new ArrayList<>();
        preconditionAsArray.add(precondition);
        minCandidates.add(preconditionAsArray);
        for (Rule r : rulesChangingPrecondition ){
            minCandidates.add(makeFire(r, logEntries));
        }
        return topsis(minCandidates, logEntries);
    }

    public ArrayList<LogEntry>  findRoots(LogEntry c, Rule rule) {
        ArrayList<LogEntry> changeablePreconditions = new ArrayList<>();
        ArrayList<Rule> activeRules = new ArrayList<>();
        //Todo: determine activeRules
        //Todo: improve naming
        for (Rule r : activeRules){
            ArrayList<LogEntry> preconditions = r.getConditions();
            preconditions.addAll(r.getTrigger());
            for(LogEntry precondition : preconditions) {
                changeablePreconditions.addAll(findRoots(precondition, r));
            }
        }
        return changeablePreconditions;
    }

    public ArrayList<LogEntry> overrideOrRemove(ArrayList<Rule> rules, LogEntry expected, Boolean firingNecessary, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<Rule> rulesHigherPriority = new ArrayList<>();
        //Todo: determine rulesHigherPriority
        for ( Rule r : rules) {
            ArrayList<LogEntry> candidate = minSubAll(rulesHigherPriority, logEntries);
            candidate.addAll(minAdd(r, expected, logEntries));
            candidates.add(candidate);
            //Todo: revelant? boolean usesDeviceInAction = false;
        }
        if (!firingNecessary) {
            candidates.add(minSubAll(rules, logEntries));
        }
        return topsis(candidates, logEntries);
    }


    /**Topsis:*/
    public ArrayList<LogEntry> topsis(ArrayList<ArrayList<LogEntry>> C, ArrayList<LogEntry> logEntries) {
       /** ArrayList<ArrayList<LogEntry>> actionableC = C;
        for (ArrayList<LogEntry> setOfPreconditions : C){
            Double A = 0.0;
            Double T = 0.0 ;
            Double size = (double)setOfPreconditions.size();
            for (LogEntry precondition : setOfPreconditions){
                //TODO: Implement actionability
                A += calculateAbnormality(precondition, logEntries);
                T += calculateTemporality(precondition);
            }
            Double abnormality = A / size;
            Double temporality = T / size;
            Double sparsity = size;
            Double proximity = calculateProximity(setOfPreconditions);
        }*/

        return C.get(0);
    }

    public Double calculateAbnormality(LogEntry c, ArrayList<LogEntry> logEntries){
        //how long was the logEntry that matches the last one with this specific entityId
        //if the state didn't change it would also be equal to the next and be counted there
        //if it is not equal, it should not continue to count
        for (LogEntry logEntry: logEntries){
            if (logEntry.equals(c)){

            }
        }
        return null;
    }

    public Double calculateTemporality(LogEntry c){
        return null;
    }

    public Double calculateProximity(ArrayList<LogEntry> setOfPreconditions){
        //find all active rules if setOfPreconditions is changed
        //analyse their actions

        return null;
    }


    /**
     * Find the logEntry that changed the device to the state it had before the explanandum
     *
     * @param explanandum explanandum determined with getExplanandumsLogEntry
     * @param logEntries  the list of Home Assistant logs
     * @return the logEntry that changed the device to the state it had before the explanandum LogEntry
     */
    public LogEntry getPreviousLogEntry(LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        String entityID = explanandum.getEntityId();
        String name = explanandum.getName();
        String state = explanandum.getState();
        String time = explanandum.getTime();

        logEntries.remove(explanandum);
        Collections.sort(logEntries, Collections.reverseOrder());

        for (LogEntry logEntry : logEntries) {
            int timeComparison = explanandum.compareTo(logEntry);
            if (timeComparison > 0 && entityID.equals(logEntry.getEntityId()) && !state.equals(logEntry.getState())) {
                return logEntry;
            }
        }
        //no previous state found:
        return null;
    }

    public Boolean falseCondition(Rule r, String stateToAchieve){
        Boolean falseCondition = false;
        ArrayList<LogEntry> conditions = r.getConditions();
        for (LogEntry condition : conditions) {
            //the condition is true if the state and entityID of the condition matches the state and entityID of the current situation.
            //the current situation is not the explanandum because it is only concerned with the correct device.
            if (!condition.getState().equals(stateToAchieve)) { //state and entityID match
                falseCondition = true;
            }
        }

        return falseCondition;
    }

    /**
     * Checks if a rule has true preconditions
     *
     * @param r             Rule for which we want to check if the preconditions are true
     * @param explanandum   the current explanandum
     * @param logEntries    list of all relevant LogEntries
     * @return
     */
    public Boolean hasTruePreconditions(Rule r, LogEntry explanandum, ArrayList<LogEntry> logEntries){
        Boolean truePreconditions = true;
        if (!findCauseSer.preconditionsApply(explanandum, r, logEntries) ) {    //no trigger was activated
            truePreconditions = false;
        }
        ArrayList<LogEntry> conditions = r.getConditions();
        ArrayList<LogEntry> currentState = getCurrentState(logEntries);
        //System.out.println(currentState);
        for ( LogEntry condition: conditions){
                if (!currentState.contains(condition)){ //condition does not have the same state and entityid as one of the elements in currentstate, i.e. the state is different
                   truePreconditions = false;
                }
                //Todo: what if a condition is concerned with a device that is not part of the logentries?
        }
        return truePreconditions;
    }

    public ArrayList<Rule> getRulesTruePreconditions(ArrayList<Rule> rules, LogEntry explanandum, ArrayList<LogEntry> logEntries){
       ArrayList<Rule> truePreconditions = new ArrayList<>();
        for (Rule rule : rules){
            if (hasTruePreconditions(rule, explanandum, logEntries)){
                truePreconditions.add(rule);
            }
        }
        return truePreconditions;
    }

    //returns a list of logEntries which contains for each entityID the newest state, i.e. the current state of this entityID
    public ArrayList<LogEntry> getCurrentState(ArrayList<LogEntry> logEntries) {
        //logEntries are already given in reverse order, i.e. from newest to oldest
        //add logEntry to new List if this entityID has not occurred before
        Collections.sort(logEntries, Collections.reverseOrder());
        ArrayList<LogEntry> currentState = new ArrayList<>();
        ArrayList<String> notYetConsideredIDs = new ArrayList<>();
        for (LogEntry entry : logEntries) { //collect all entityIds
            String id = entry.getEntityId();
            if (!notYetConsideredIDs.contains(id)) {
                notYetConsideredIDs.add(id);
            }
        }
        for (LogEntry entry : logEntries) {
            String id = entry.getEntityId();
            if(notYetConsideredIDs.contains(id)) {
                currentState.add(entry);
                notYetConsideredIDs.remove(id);
            }
        }
        return currentState;
    }
}
