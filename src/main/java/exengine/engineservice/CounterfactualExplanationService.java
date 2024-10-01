package exengine.engineservice;

import java.lang.reflect.Array;
import java.util.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.apache.juli.logging.Log;
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

        //Determine explanandum / current state:
        LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);
        if (explanandum == null) {
            return "No explanandum found. Could not proceed.";
        }
        LOGGER.info("Found explanandum: {}", explanandum);
        LOGGER.info("Found stateCurrent: {}", explanandum.getState());
        String entityId = explanandum.getEntityId();


        // Determine previous state:
        LogEntry previous = getPreviousLogEntry(explanandum, logEntries);   // = explanandum if there is no previous LogEntry
        LOGGER.info("Found previous: {}", previous);
        LOGGER.info("Found statePrevious: {}", previous.getState());

        // Determine expected state:
        LogEntry expected = explanandum;
        Object happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
        ArrayList<Rule> candidateRules = contrastiveSer.getCandidateRules(entityId);
        Rule expectedRule = contrastiveSer.getExpectedRule(explanandum, logEntries, candidateRules, happenedEvent, device, entityId, userId);
        if (expectedRule == null) {
            return "Error, the foil could not be found.";
        }
        ArrayList<LogEntry> expectedCandidates = new ArrayList<>(expectedRule.getActions());
        for (LogEntry logEntry : expectedCandidates) {
            if (logEntry.getEntityId().equals(entityId)) {    //it is the action of the rule we want
                expected = logEntry;
            }
        }


        //Determine rules with currently true preconditions:
        ArrayList<LogEntry> currentState = new ArrayList<>(getCurrentState(logEntries));

        ArrayList<Rule> rulesCurrent = findCauseSer.findCandidateRules(explanandum, dbRules);  //rules with actions leading to current state
        ArrayList<Rule> rulesPrevious = findCauseSer.findCandidateRules(previous, dbRules);  //rules with actions leading to previous state
        ArrayList<Rule> rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);  //rules with actions leading to expected state
        TruePreconditions(rulesCurrent, explanandum, currentState, logEntries);    //checks if the conditions currently are true and a trigger has activated before the explanandum
        //Todo: What about mixed cases where the trigger has fired, but the conditions were not true yet?
        TruePreconditions(rulesPrevious, explanandum, currentState, logEntries);
        TruePreconditions(rulesExpected, explanandum, currentState, logEntries);

        //Only consider rules with higher priority in rulesPrevious:
        if (!rulesExpected.isEmpty()) {
            Rule maxPrioRule = Collections.max(rulesExpected, Comparator.comparingInt(Rule::getPriority));
            int maxPrio = maxPrioRule.getPriority();
            rulesPrevious.removeIf(r -> r.getPriority() <= maxPrio);

        }


        // Case Distinction:
        boolean firingNecessary = true;
        ArrayList<ArrayList<LogEntry>> minPreconditions = new ArrayList<>();    //first Array contains additive, second subtractive information
        if (!rulesCurrent.isEmpty()) {
            if (!rulesPrevious.isEmpty() || expected.equals(previous)) {
                firingNecessary = false;
            }
            rulesCurrent.addAll(rulesPrevious);
            minPreconditions.addAll(overrideOrRemove(rulesCurrent, explanandum, expected, firingNecessary, logEntries));
        } else {
            if (!rulesExpected.isEmpty()) {
                Rule dummy = new Rule("dummy", null, null, null, null, null, null, 0);
                minPreconditions.add(minAdd(dummy, expected, explanandum, logEntries));
            } else {
                return "Error, there is no explanation need.";
            }
        }

        LOGGER.info("Explanation generated");
        return generateCFE(minPreconditions, explanandum, expected);

    }


    public String generateCFE(ArrayList<ArrayList<LogEntry>> minPreconditions, LogEntry explanandum, LogEntry expected) {
        String explanation = expected.getName() + " would have occurred instead of " + explanandum.getName() + " if in the past ";
        ArrayList<LogEntry> addPreconditions = minPreconditions.get(0);
        ArrayList<LogEntry> subPreconditions = minPreconditions.get(1);

        //Todo: remove oxford comma if length is <=2
        //add additive preconditions to explanation:
        for (int i = 0; i < addPreconditions.size() - 1; i++) {
            explanation = explanation.concat(addPreconditions.get(i).getName());
            explanation = explanation.concat(", ");
        }
        explanation = explanation.concat("and ");
        explanation = explanation.concat(addPreconditions.get(addPreconditions.size() - 1).getName());
        explanation = explanation.concat(" had happened and ");

        //add subtractive preconditions to explanation:
        for (int i = 0; i < subPreconditions.size() - 1; i++) {
            explanation = explanation.concat(subPreconditions.get(i).getName());
            explanation = explanation.concat(", ");
        }
        explanation = explanation.concat("and ");
        explanation = explanation.concat(addPreconditions.get(addPreconditions.size() - 1).getName());
        explanation = explanation.concat("had not happened.");

        //Capitalize only the first letter:
        explanation = explanation.toLowerCase();
        explanation = explanation.substring(0, 1).toUpperCase() + explanation.substring(1);

        return explanation;
    }

    // Subtractive Methods:

    /**
     * Finds the minimal set of LogEntries to change s.t.  ruleToReverse does not have true preconditions anymore.
     *
     * @param ruleToReverse the rule that is to be reversed
     * @param explanandum   the action that is to be explained
     * @param logEntries    the list of Home Assistant logs
     * @return the minimal set of LogEntries s.t. reversing them prevents ruleToReverse from being active
     */
    public ArrayList<LogEntry> minSub(Rule ruleToReverse, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        ArrayList<LogEntry> toReverse = new ArrayList<>();
        ArrayList<LogEntry> preconditions = new ArrayList<>(ruleToReverse.getConditions());
        preconditions.addAll(ruleToReverse.getTrigger());
        ArrayList<LogEntry> actionablePreconditions = new ArrayList<>();
        ArrayList<LogEntry> mutablePreconditions = new ArrayList<>();
        ArrayList<LogEntry> nonMutablePreconditions = new ArrayList<>();

        // Sort by controllability
        for (LogEntry precondition : preconditions) {
            String controllability = getControllabilityByEntityId(precondition.getEntityId());
            switch (controllability) {
                case "actionable" -> actionablePreconditions.add(precondition);
                case "mutable" -> mutablePreconditions.add(precondition); //mutable but non-actionable
                case "non-mutable" -> nonMutablePreconditions.add(precondition);
                default -> {
                    LOGGER.error("Error, an entity has invalid controllability");
                    return null;        //Todo: Check what happens in this case
                }
            }
        }

        // actionable preconditions
        for (LogEntry precondition : actionablePreconditions) {
            toReverse.addAll(new ArrayList<>(findRoots(precondition, explanandum, logEntries)));
        }

        // mutable but non-actionable preconditions
        for (LogEntry precondition : mutablePreconditions) {
            toReverse.addAll(new ArrayList<>(findRoots(precondition, explanandum, logEntries)));
            toReverse.remove(precondition); // precondition cannot be directly manipulated
        }

        // consider non-mutable preconditions if there are no other ones
        if (toReverse.isEmpty()) {
            toReverse.addAll(nonMutablePreconditions);
            LOGGER.info("There are no mutable preconditions. Proceed with non-mutable ones");
        }

        noDuplicates(toReverse);

        // findRoots considers rules with true preconditions
        // but the manipulation can use a rule that that does not have true preconditions
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();
        for (LogEntry precondition : toReverse) {
            minCandidates.add(modify(precondition, explanandum, logEntries));
        }

        noDuplicates(minCandidates);
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
        ArrayList<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());

        // Find rules that lead to the desired state
        ArrayList<Rule> candidateRules = findCauseSer.findCandidateRules(toAchieve, dbRules);
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
        noDuplicates(candidates);
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
            // remove invalid conditions
            if (p.getEntityId() == null || p.getState() == null) {
                i.remove();
            } else {
                for (LogEntry state : currentStates) {
                    if (p.equals(state)) {  //check if the preconditions are already true
                        i.remove();
                    }
                }
            }
        }

        // check if it is more minimal to fire a rule instead of manipulating the condition directly
        ArrayList<LogEntry> modifiedConditions = new ArrayList<>();
        for (LogEntry condition : conditions) {
            ArrayList<LogEntry> modifiedCondition = modify(condition, explanandum, logEntries);
            if (modifiedCondition != null && !modifiedCondition.isEmpty()) {
                modifiedConditions.addAll(modifiedCondition);
            }
        }

        ArrayList<LogEntry> minPreconditions = new ArrayList<>(modifiedConditions);


        // find false triggers of r
        boolean hasTrueTrigger = findCauseSer.preconditionsApply(explanandum, r, logEntries);

        // all triggers are false, if one is true we don't need to change the triggers
        if (!hasTrueTrigger) {
            ArrayList<ArrayList<LogEntry>> triggerCandidates = new ArrayList<>();
            for (LogEntry t : triggers) {
                // remove invalid triggers
                if (t.getEntityId() == null || t.getState() == null) {
                    i.remove();
                }
                ArrayList<LogEntry> triggerCandidate = modify(t, explanandum, logEntries);
                if (triggerCandidate != null && !triggerCandidate.isEmpty()) {
                    triggerCandidates.add(triggerCandidate);
                }
            }
            // find minimum trigger:
            noDuplicates(triggerCandidates);
            ArrayList<LogEntry> minTrigger = minComputation(triggerCandidates, explanandum, logEntries);
            minPreconditions.addAll(minTrigger);
        }
        //todo: remove duplicates in all methods


        noDuplicates(minPreconditions);
        return minPreconditions;
    }

    /**
     * @param precondition Condition or Trigger of a rule that contain the state the system should have
     *                     to make the rule fire.
     * @param logEntries   List of all logEntries that are considered
     * @return Minimal set of LogEntries with states that have to be changed to the mentioned state to make the logEntry precondition true
     */
    public ArrayList<LogEntry> modify(LogEntry precondition, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        //System.out.println("method modify called with precondition: " + precondition.getName());
        ArrayList<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());
        ArrayList<Rule> rulesChangingPrecondition = new ArrayList<>();
        for (Rule r : dbRules) {
            if (r.getActions().contains(precondition)) {   //there exists an action of r s.t. state and entityID of this action are the same as the ones of precondition,
                // i.e. firing this rule would lead to this state in this entityID
                rulesChangingPrecondition.add(r);
            }
        }
        ArrayList<ArrayList<LogEntry>> minCandidates = new ArrayList<>();
        ArrayList<LogEntry> preconditionAsArray = new ArrayList<>();
        preconditionAsArray.add(precondition);
        minCandidates.add(preconditionAsArray);
        for (Rule r : rulesChangingPrecondition) {
            //System.out.println("Candidate rule in modify found:" + r.getRuleName());
            // System.out.println("With the rule the following is added to mincandidates:" + makeFire(r, explanandum, logEntries));
            minCandidates.add(makeFire(r, explanandum, logEntries));

        }
        noDuplicates(minCandidates);
        return minComputation(minCandidates, explanandum, logEntries);
    }

    /**
     * Supposes that c is mutable. If there is no rule that fires c, it has to be actionable, because it cannot be mutable and non-actionable.
     *
     * @param c
     * @param explanandum
     * @param logEntries
     * @return all preconditions that can be directly manipulated to make precondition c false (including c if c can be directly manipulated)
     */
    public ArrayList<LogEntry> findRoots(LogEntry c, LogEntry explanandum, ArrayList<LogEntry> logEntries) {
        //System.out.println("Find roots for the following precondition calculated: " + c.getName());

        ArrayList<Rule> dbRules = new ArrayList<>(dataSer.findAllRules());
        ArrayList<LogEntry> currentState = new ArrayList<>(getCurrentState(logEntries));
        if (!currentState.contains(c)) {      //c is already false, nothing has to be changed
            return new ArrayList<>();
        }


        //determine Rules that prevent us from directly changing c:
        ArrayList<Rule> activeRules = findCauseSer.findCandidateRules(c, dbRules);   //Rules that lead to c being true
        TruePreconditions(activeRules, explanandum, currentState, logEntries);
        //System.out.println("The active rules for precondition" + c.getName() + " are " + activeRules);

        ArrayList<LogEntry> changeablePreconditions = new ArrayList<>();

        //Todo: improve naming
        //Todo: go through test again
        //Todo: Add this check to all methods

        if (activeRules == null || activeRules.isEmpty()) {  //we can directly change c
            changeablePreconditions.add(c);
        } else {
            for (Rule r : activeRules) {
                ArrayList<LogEntry> preconditions = new ArrayList<>(r.getConditions());
                preconditions.addAll(r.getTrigger());
                //System.out.println("preconditions of active rule:" + r.getRuleName() + " are " + preconditions);
                for (LogEntry precondition : preconditions) {
                    ArrayList<LogEntry> roots = new ArrayList<>(findRoots(precondition, explanandum, logEntries));
                    changeablePreconditions.addAll(roots);
                }
            }
        }
        noDuplicates(changeablePreconditions);
        // System.out.println(" The roots for  preconditions" + c.getName() + " are " + changeablePreconditions);
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
                System.out.println("the subtractive part is:" + subtracting);
                candidate.addAll(subtracting);
                subCandidates.addAll(candidate);
            }

            //additive part:
            ArrayList<LogEntry> overriding = minAdd(r, explanandum, expected, logEntries);
            System.out.println("the additive part is:" + overriding);
            if (overriding != null && !overriding.isEmpty()) {
                candidate.addAll(overriding);
                addCandidates.addAll(overriding);
            }

            candidates.add(candidate);
        }

        //removing all rules and not firing any:
        if (!firingNecessary) {
            ArrayList<LogEntry> subtractingAll = minSubAll(rulesSorted, explanandum, logEntries);
            System.out.println("The all subtractive part is:" + subtractingAll);
            candidates.add(subtractingAll);
            subCandidates.addAll(subtractingAll);
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
        return null;

        //dummy return:
        /**ArrayList<LogEntry> dummy = new ArrayList<>();
         for (ArrayList<LogEntry> candidate : candidates) {
         if (!candidate.isEmpty()) {
         dummy.add(candidate.get(0));
         }
         }
         System.out.println("dummy has been caluclated (should be the first entry of every candidate:" + dummy + " the first entry has name + " + dummy.get(0).getEntityId() + dummy.get(0).getState());
         return dummy;   //first entry of every option

         /**  //check for actionability:
         noDuplicates(candidates);
         ArrayList<ArrayList<LogEntry>> actionableCandidates = new ArrayList<>();
         for (ArrayList<LogEntry> candidate : candidates){
         ArrayList<LogEntry> actionableCandidate = new ArrayList(candidate);
         actionableCandidates.add(actionableCandidate);
         for (LogEntry c: candidate){
         if ( !getControllabilityByEntityId(c.getEntityId()).equals("actionable")){ //Todo: Remove all rules with null entityID
         actionableCandidates.remove(actionableCandidate);
         }
         }
         }

         if (!actionableCandidates.isEmpty()){
         candidates = actionableCandidates;
         }

         //calculate properties
         ArrayList<Double> abnormality = calculateAbnormality(candidates, logEntries);
         ArrayList<Double> temporality = calculateAbnormality(candidates, logEntries);
         ArrayList<Double> proximity = calculateProximity(candidates, explanandum,logEntries);
         ArrayList<Double> sparsity = calculateSparsity(candidates);


         ArrayList<Double> weights = new ArrayList<Double>();
         weights.add(ABNORMALITY_WEIGHT);
         weights.add(TEMPORALITY_WEIGHT);
         weights.add(PROXIMITY_WEIGHT);
         weights.add(SPARSITY_WEIGHT);

         ArrayList<Boolean> isBeneficial = new ArrayList<>();
         isBeneficial.add(ABNORMALITY_BENEFICIAL);
         isBeneficial.add(TEMPORALITY_BENEFICIAL);
         isBeneficial.add(PROXIMITY_BENEFICIAL);
         isBeneficial.add(SPARSITY_BENEFICIAL);

         ArrayList<LogEntry> minimum = ContrastiveExplanationService.topsis(candidates, weights, isBeneficial, abnormality, temporality,
         proximity, sparsity);

         if (minimum == null) {
         LOGGER.error("Error in TOPSIS Calculation");
         return null;
         }




         /* noDuplicates(candidates);
         ArrayList<ArrayList<LogEntry>> actionableCandidates = candidates;
         ArrayList<Double> abnormality = new ArrayList<>(candidates.size());
         ArrayList<Double> temporality = new ArrayList<>(candidates.size());
         ArrayList<Double> sparsity = new ArrayList<>(candidates.size());
         ArrayList<Double> proximity = new ArrayList<>(candidates.size());
         System.out.println(candidates.size());
         for (int i = 0; i < candidates.size(); i++){
         ArrayList<LogEntry> setOfPreconditions = candidates.get(i);
         double A = 0.0;
         double T = 0.0 ;
         double size = (double)setOfPreconditions.size();
         for (LogEntry precondition : setOfPreconditions){
         //TODO: Implement actionability
         A += calculateAbnormality(precondition, logEntries);
         T +=  calculateTemporality(precondition, explanandum, logEntries);
         }
         abnormality.set(i,  A / size);
         temporality.set(i, T / size);
         sparsity.set(i, size);
         proximity.set( i, calculateProximity(setOfPreconditions, explanandum, logEntries));
         }

         ArrayList<LogEntry> minimal = candidates.get(candidates.size()-1);
         // System.out.println("The preconditions that are chosen  to be minimal with topsis are: "+  minimal);
         return minimal;*/


    }

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
            System.out.println("Sum " + sum);
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
                        System.out.println("Newer LogEntry detected.");
                    }
                }
                if (newest.compareTo(explanandum) > 0) {  //no identical logEntry before explanandum found
                    LOGGER.info("For LogEntry " + newest.getEntityId() + " with state " + newest.getState() + " no entry before the explanandum could be found. The temporality has been set to max.");
                    sum = Integer.MAX_VALUE;
                    break;
                } else {
                    LocalDateTime current = explanandum.getLocalDateTime();
                    LocalDateTime last = newest.getLocalDateTime();
                    //  System.out.println("Time difference calculated for LogEntry" + newest.getEntityId() + newest.getState() + ". It is :" + (double) last.until(current, SECONDS));
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

            // state of the system after the manipulations of this iteration are included
            ArrayList<LogEntry> stateAfter = new ArrayList<>(getCurrentState(logEntries));

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


                for (Rule r : activeRules) {
                    System.out.println(" the following rule is active: " + r.getRuleName());
                }


                // collect all changes to the system from the active rules in the changesIteration array
                for (Rule r : activeRules) {
                    changesIteration.addAll(r.getActions());
                }
                noDuplicates(changesIteration);
                System.out.println("ChangesInteraction contains:" + changesIteration);

                //remove entry if it is already in stateBefore
                Iterator<LogEntry> i = changesIteration.iterator();
                while (i.hasNext()) {
                    LogEntry changeIteration = i.next();
                    if (stateCurrent.contains(changeIteration)) {
                        i.remove();
                        System.out.println("change is removed because it is already in the current state" + changeIteration.getName());
                    }
                }

                for (LogEntry change : changesIteration) {
                    System.out.println(" the following change is added:" + change.getName());
                }
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
                            System.out.println("the following entry is removed from current state " + current.getName());
                            System.out.println("the following entry is added to current state " + changeIteration.getName());
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

//Todo: calculate changes and then based on priority remove some and then abgleichen mit current state
//Todo: Check which way around the logEntries are given, the ones to change how they are currently or how they should be?


    /**
     * Find the logEntry that changed the device to the state it had before the explanandum
     *
     * @param explanandum explanandum determined with getExplanandumsLogEntry
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
        boolean truePreconditions = findCauseSer.preconditionsApply(explanandum, r, logEntries);
        ArrayList<LogEntry> conditions = r.getConditions();
        for (LogEntry condition : conditions) {
            if (condition != null && condition.getEntityId() != null && !currentState.contains(condition)) { //condition does not have the same state and entityid as one of the elements in currentstate, i.e. the state is different and does not have entityId null which makes it an invalid condition
                truePreconditions = false;
                break;
            }
            //Todo: what if a condition is concerned with a device that is not part of the logentries?
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
        return dataSer.findEntityByEntityID(entityId).getControllability();
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
                Rule maxPrioRule = Collections.max(sameAction, Comparator.comparingInt(Rule::getPriority));
                sameAction.remove(maxPrioRule);
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


}
