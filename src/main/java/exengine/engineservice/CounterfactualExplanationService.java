package exengine.engineservice;

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

        String stateCurrent = null;
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
            rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);  //rules with actions leading to state_current
        }

        Boolean firingNecessary = true;
        ArrayList<LogEntry> minPreconditions = new ArrayList<>();
        if (!rulesCurrent.isEmpty()) {
            if (!rulesPrevious.isEmpty() || stateExpected == statePrevious){
               firingNecessary = false;
            }
                rulesCurrent.addAll(rulesPrevious);
            minPreconditions = overrideOrRemove(rulesCurrent, stateExpected, firingNecessary);
        } else {
            if (!rulesExpected.isEmpty()){
                minPreconditions = minAdd(null, stateExpected);
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
    public ArrayList<LogEntry> minSub(Rule ruleToReverse) {
        ArrayList<LogEntry> Csub = new ArrayList<>();
        ArrayList<ArrayList<LogEntry>> CsubArray = new ArrayList<>();
        ArrayList<LogEntry> C = ruleToReverse.getConditions();
        C.addAll(ruleToReverse.getTrigger());
        /** TODO: Implement mutablility*/
        for (LogEntry c : C){
            Csub.addAll(findRoots(c,ruleToReverse));
        }
        for (LogEntry csub : Csub){
            ArrayList<LogEntry> updatedcsub = modify(csub);
            CsubArray.add(updatedcsub);

        }
        return topsis(CsubArray);
    }

    public ArrayList<LogEntry> minSubAll(ArrayList<Rule> rulesToReverse){
        ArrayList<LogEntry> Csub = new ArrayList<>();
        for(Rule r : rulesToReverse){
            Csub.addAll(minSub(r));
        }
        return Csub;
    }


    /**Additive Method:*/
    public ArrayList<LogEntry> minAdd(Rule ruleToOverride, String stateExpected) {
        if (ruleToOverride.getRuleId().isEmpty()){
            //TODO: Set Priority to 0, check that condition is correct
        }
        //TODO: Determine rulesAdd
        ArrayList<Rule> rulesAdd = new ArrayList<>();
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        for (Rule r : rulesAdd){
            ArrayList<LogEntry> candidate = makeFire(r);
            if (candidate.size() > 3){
                rulesAdd.remove(r);
            }
        }
        if (rulesAdd.isEmpty()){
            LOGGER.info("No additive explanation available. No rule that could fire found.");
            return null;
        }
        return topsis(candidates);
    }


    /**Auxilary Methods:*/

    public ArrayList<LogEntry> makeFire(Rule r) {
        ArrayList<LogEntry> preconditions = r.getConditions();
        ArrayList<LogEntry> triggers = r.getTrigger();
        //Todo: Only take false ones, or is it not relevant?
        ArrayList<LogEntry> updatedPreconditions = new ArrayList<>();
        ArrayList<LogEntry> updatedTriggers = new ArrayList<>();
        for (LogEntry c : preconditions){
            updatedPreconditions.addAll(modify(c));
        }
        for (LogEntry c : triggers) {
            updatedTriggers.addAll(modify(c));
        }
        r.setConditions(updatedPreconditions);
        r.setTrigger(updatedTriggers);
        //Todo: Check that r is really updated after the method
        //todo: remove duplicates in all methods
        updatedPreconditions.addAll(updatedTriggers);
        return updatedPreconditions;
    }

    public ArrayList<LogEntry> modify(LogEntry c) {
        //Todo: Find R
        ArrayList<Rule> R = new ArrayList<>();
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();
        ArrayList<LogEntry> cAsArray = new ArrayList<>();
        cAsArray.add(c);
        for (Rule r : R ){
            minCandidates.add(makeFire(r));
        }
        minCandidates.add(cAsArray);
        return topsis(minCandidates);
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

    public ArrayList<LogEntry> overrideOrRemove(ArrayList<Rule> rules, String stateToAchieve, Boolean firingNecessary ) {
        ArrayList<ArrayList<LogEntry>> candidates = new ArrayList<>();
        ArrayList<Rule> rulesHigherPriority = new ArrayList<>();
        //Todo: determine rulesHigherPriority
        for ( Rule r : rules) {
            ArrayList<LogEntry> candidate = minSubAll(rulesHigherPriority);
            candidate.addAll(minAdd(r, stateToAchieve));
            candidates.add(candidate);
            //Todo: revelant? boolean usesDeviceInAction = false;
        }
        if (!firingNecessary) {
            candidates.add(minSubAll(rules));
        }
        return topsis(candidates);
    }


    /**Topsis:*/
    public ArrayList<LogEntry> topsis(ArrayList<ArrayList<LogEntry>> C) {
        ArrayList<ArrayList<LogEntry>> actionableC = C;
        for (ArrayList<LogEntry> setOfPreconditions : C){
            Double A = 0.0;
            Double T = 0.0 ;
            Double size = (double)setOfPreconditions.size();
            for (LogEntry precondition : setOfPreconditions){
                //TODO: Implement actionability
                A += calculateAbnormality(precondition);
                T += calculateTemporality(precondition);
            }
            Double abnormality = A / size;
            Double temporality = T / size;
            Double sparsity = size;
            Double proximity = calculateProximity();
        }

        return C.get(0);
    }

    public Double calculateAbnormality(LogEntry c){
        return null;
    }

    public Double calculateTemporality(LogEntry c){
        return null;
    }

    public Double calculateProximity(){
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

        ArrayList<LogEntry> actions = dataSer.getAllActions();

        String entityID = explanandum.getEntityId();
        String name = explanandum.getName();
        String state = explanandum.getState();
        String time = explanandum.getTime();

        logEntries.remove(explanandum);

        //sort the logEntries s.t. the newest ones are the first
        Collections.sort(logEntries, Collections.reverseOrder());

        //similar to getExplanandumsLogEntry, find the newest logEntry that is before the explanandum LogEntry
        //and has the same entityID as the explanandum but different state

        for (LogEntry logEntry : logEntries) {
            int timeComparison = explanandum.compareTo(logEntry);
            //logEntry is found if it happened before the explanandum, the entitiyID is the same and the state is differnet
            /** TODO: Check that > 0 is the correct direction */
            if (timeComparison > 0 && entityID == logEntry.getEntityId() && state != logEntry.getState()) {
                return logEntry;
            }
        }
        //no previous state found:
        return null;
    }








}
