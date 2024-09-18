package exengine.engineservice;

import java.lang.reflect.Array;
import java.util.*;
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
            rulesCurrent = TruePreconditions(rulesCurrent, explanandum, logEntries);
            //Todo: Is previous correct here?
            rulesPrevious = TruePreconditions(rulesPrevious, previous, logEntries);
            rulesExpected = TruePreconditions(rulesExpected, expected, logEntries);




        }

        /** Find method based on case:*/

        Boolean firingNecessary = true;
        ArrayList<LogEntry> minPreconditions = new ArrayList<>();
        if (!rulesCurrent.isEmpty()) {
            if (!rulesPrevious.isEmpty() || stateExpected == statePrevious){
               firingNecessary = false;
            }
                rulesCurrent.addAll(rulesPrevious);
            minPreconditions = overrideOrRemove(rulesCurrent, explanandum, expected, firingNecessary, logEntries);
        } else {
            if (!rulesExpected.isEmpty()){
                Rule dummy = new Rule("dummy", null, null, null, null, null, null, 0);
                minPreconditions = minAdd(dummy, expected, logEntries);
            } else {
                LOGGER.info("Error, there is no explanation need");
            }
        }

        LOGGER.info("Explanation generated");
        return generateCFE(minPreconditions);

    }


    public String generateCFE(Object minPrecondition){
        return "counterfactual explanation";
    }

    /**Subtractive Methods:*/
    public ArrayList<LogEntry> minSub(Rule ruleToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> toReverse = new ArrayList<>();
        ArrayList<ArrayList<LogEntry>> toReverseArray = new ArrayList<>();
        ArrayList<LogEntry> preconditions = ruleToReverse.getConditions();
        preconditions.addAll(ruleToReverse.getTrigger());
        /** TODO: Implement mutability*/
        for (LogEntry precondition : preconditions){
            toReverse.addAll(findRoots(precondition,ruleToReverse, explanandum, logEntries));
        }
        for (LogEntry precondition : toReverse){
            ArrayList<LogEntry> updatedcsub = modify(precondition, logEntries);
            toReverseArray.add(updatedcsub);

        }
        return topsis(toReverseArray, logEntries);
    }

    public ArrayList<LogEntry> minSubAll(ArrayList<Rule> rulesToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries){
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> conditions = new ArrayList<>();
        for(Rule r : rulesToReverse){
            conditions.addAll(minSub(r ,explanandum, logEntries));
        }
        return conditions;
    }


    /**Additive Method:*/
    public ArrayList<LogEntry> minAdd(Rule ruleToOverride, LogEntry expected, ArrayList<LogEntry>logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<Rule> candidateRules = findCauseSer.findCandidateRules(expected, dbRules);
        Iterator<Rule> i = candidateRules.iterator();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        while (i.hasNext()) {
            Rule r = i.next();
            System.out.println("candidate Rule for additive: " + r.getRuleName());
            ArrayList<LogEntry> candidate = makeFire(r, logEntries);
            if (r.getPriority() > ruleToOverride.getPriority() && candidate.size() <= 3){
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()){
            LOGGER.info("No additive explanation available. No rule that could fire found.");
            return null;
        }
        return topsis(candidates, logEntries);
    }


    /**Auxilary Methods:*/

    /**
     * Determines the states and EntityIds that need to be changed to make rule r fire
     *
     * @param r             Rule that has to be fired
     * @param logEntries    List of all relevant Logentries
     * @return              List of LogEntries which contain the states the system has to have to make r fire
     */
    public ArrayList<LogEntry> makeFire(Rule r, ArrayList<LogEntry> logEntries) {
        //System.out.println("MakeFire entered with rule" + r.getRuleName() + ". It has id: " + r.getRuleId());
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> currentStates = getCurrentState(logEntries);
        ArrayList<LogEntry> preconditions = r.getConditions();
        ArrayList<LogEntry> triggers = r.getTrigger();
        Iterator<LogEntry> i = preconditions.iterator();

            while (i.hasNext()) {
                LogEntry p = i.next();
                if (p.getEntityId() == null || p.getState() == null) {   //p is not a valid precondition
                    i.remove();
                } else {
                    for (LogEntry state : currentStates) {
                       if (p.equals(state)) {  //check if the preconditions are already true
                            i.remove();
                            // System.out.println("The precondition that is checked if it is true is: " + p);
                            //System.out.println("The state that it is checked against is: " + state);
                            //System.out.println("The precondition " + p + "with id " + p.getEntityId() +" of rule " + r.getRuleName() + " is excluded because it is already true" );
                        }
                    }
                }
            }
        Iterator<LogEntry> j = triggers.iterator();
        while (j.hasNext()){
            LogEntry t = j.next();
            for (LogEntry state : currentStates){
                if(t.equals(state)){  //check if the preconditions are already true
                    j.remove();
                }
            }
        }
        ArrayList<LogEntry> updatedPreconditions = new ArrayList<>();
        ArrayList<LogEntry> updatedTriggers = new ArrayList<>();
        for (LogEntry c : preconditions){
            updatedPreconditions.addAll(modify(c, logEntries));
        }
        for (LogEntry t : triggers) {
            updatedTriggers.addAll(modify(t, logEntries));
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
        //LOGGER.info("method modify called with precondition: " + precondition);
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
      //  System.out.println("Modify for preconditions " + precondition +  " is calculated. The candidates are: " + minCandidates);
        return topsis(minCandidates, logEntries);
    }

    public ArrayList<LogEntry>  findRoots(LogEntry c, Rule rule, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        List<Rule> dbRules = dataSer.findAllRules();
        ArrayList<LogEntry> changeablePreconditions = new ArrayList<>();
        ArrayList<Rule> candidateRules = findCauseSer.findCandidateRules(c, dbRules);   //Rules that lead to c being true
        candidateRules = TruePreconditions(candidateRules, explanandum, logEntries);
        //Todo: improve naming
        for (Rule r : candidateRules){
            ArrayList<LogEntry> preconditions = r.getConditions();
            preconditions.addAll(r.getTrigger());
            for(LogEntry precondition : preconditions) {
                changeablePreconditions.addAll(findRoots(precondition, r, explanandum, logEntries));
            }
        }
        return changeablePreconditions;
    }

    public ArrayList<LogEntry> overrideOrRemove(ArrayList<Rule> rules, LogEntry explanandum, LogEntry expected, Boolean firingNecessary, ArrayList<LogEntry> logEntries) {
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        // Sort the ArrayList by priority in descending order:
        Collections.sort(rules, new Comparator<Rule>() {
            @Override
            public int compare(Rule r1, Rule r2) {
                // Sort in descending order
                return Integer.compare(r2.getPriority(), r1.getPriority());
            }
        });
        for (int i = 0; i< rules.size(); i++) {
            Rule r = rules.get(i);
            ArrayList<LogEntry> candidate = new ArrayList<>();
            if (i != 0){    //r has not the highest priority, i.e. there is a subtractive part
                List<Rule> rulesSublist = rules.subList(0, i-1);
                ArrayList<Rule> rulesHigherPriority = new ArrayList<>(rulesSublist);
                candidate = minSubAll(rulesHigherPriority, explanandum, logEntries);
            }
            candidate.addAll(minAdd(r, expected, logEntries));
            candidates.add(candidate);
        }
        if (!firingNecessary) {
            candidates.add(minSubAll(rules, explanandum, logEntries));
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

       for (int i= 0; i < C.size(); i++){
          // System.out.println("One topsis candidate is: " + C.get(i));
       }
       ArrayList<LogEntry> minimal = C.get(C.size()-1);
      // System.out.println("The preconditions that are chosen  to be minimal with topsis are: "+  minimal);
        return minimal;
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

    public ArrayList<Rule> TruePreconditions(ArrayList<Rule> rules, LogEntry explanandum, ArrayList<LogEntry> logEntries){
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



    /**
     * Removes duplicate elements from a list while preserving the order of the
     * elements.
     *
     * @param <T>  the type of elements in the list
     * @param list the list to remove duplicates from
     * @return the list with duplicates removed
     */
    private <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

        Set<T> set = new LinkedHashSet<>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }


}
