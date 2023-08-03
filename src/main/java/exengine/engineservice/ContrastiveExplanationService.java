package exengine.engineservice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;

/**
 * Service hub responsible for building and delivering <b>contrastive</b>
 * explanations.
 */
@Service
public class ContrastiveExplanationService extends ExplanationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContrastiveExplanationService.class);

	@Autowired
	DatabaseService dataSer;

	@Autowired
	FindCauseService findCauseSer;

	final int FREQUENCY_THRESHOLD = 90;
	final int OCCURRENCE_THRESHOLD = 90;

	final Double PRECONDITION_WEIGHT = 1.0;
	final Boolean PRECONDITION_BENEFICIAL = true;
	final Double OWNERSHIP_WEIGHT = 1.0;
	final Boolean OWNERSHIP_BENEFICIAL = true;
	final Double FREQUENCY_WEIGHT = 1.0;
	final Boolean FREQUENCY_BENEFICIAL = true;
	final Double OCCURRENCE_WEIGHT = 1.0;
	final Boolean OCCURRENCE_BENEFICIAL = true;

	/**
	 * Builds context-specific <b>contrastive</b> explanations for home assistant.
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

		/*
		 * GENERAL
		 * 
		 * Vergleich von preconditions anhand von name / entity_id
		 * 
		 * conditions werden als LogEntry behandelt (obwohlvon homeAssistant eig keine
		 * sind)
		 * 
		 * TODO logging / good comments
		 * 
		 * TODO check entity IDs for conditions in rule yaml
		 * 
		 * TODO add rules for contrastive explanations in rule yaml
		 */

		String explanation = "found nothing to explain"; // default return value

		ArrayList<LogEntry> logEntries = getLogEntries(min);

		// identify explanandum's LogEntry
		LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);

		Object happenedEvent = null;

		if (explanandum != null) { // an event happened
			List<Rule> dbRules = dataSer.findAllRules();
			List<Error> dbErrors = dataSer.findAllErrors();
			happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
		}

		// get rules that have actions with device (get all rules and remove those that
		// don't use device)
		ArrayList<Rule> mostLikelyRuleCandidates = (ArrayList<Rule>) dataSer.findAllRules(); // find all rules
		for (Rule r : mostLikelyRuleCandidates) {
			boolean usesDeviceInAction = false;
			for (LogEntry action : r.getActions()) {
				if (action.getEntityId().equals(device)) {
					usesDeviceInAction = true;
				}
			}
			if (!usesDeviceInAction) {
				mostLikelyRuleCandidates.remove(r); // remove if rule doesn't have an action with device
			}
		}

		// declare expectedRule variable
		Rule expectedRule = null;

		if (happenedEvent != null) { // explanandum & cause were found
			if (happenedEvent instanceof Rule) { // a rule has happened
				// CASE RULE HAPPENED

				mostLikelyRuleCandidates.remove(happenedEvent); // remove happened Rule from the candidate list for most
																// likely rules
				// as of now, mostLikelyRuleCandidates contains all rules, that use device in at
				// least one action besides the happened rule

				// calculate Precondition similarity
				ArrayList<Double> preconditionSimList = calculatePreconditionSimilarity(mostLikelyRuleCandidates,
						(Rule) happenedEvent);

				// calculate Ownership
				ArrayList<Double> ownershipList = calculateOwnership(mostLikelyRuleCandidates, userId);

				// calculate frequency
				ArrayList<Double> frequencyList = calculateFrequency(mostLikelyRuleCandidates, FREQUENCY_THRESHOLD);

				// calculate occurrence
				ArrayList<Double> occurrenceList = calculateOccurrence(mostLikelyRuleCandidates, OCCURRENCE_THRESHOLD,
						userId);

				ArrayList<Double> weights = new ArrayList<Double>();
				weights.add(PRECONDITION_WEIGHT);
				weights.add(OWNERSHIP_WEIGHT);
				weights.add(FREQUENCY_WEIGHT);
				weights.add(OCCURRENCE_WEIGHT);

				ArrayList<Boolean> isBeneficial = new ArrayList<Boolean>();
				isBeneficial.add(PRECONDITION_BENEFICIAL);
				isBeneficial.add(OWNERSHIP_BENEFICIAL);
				isBeneficial.add(FREQUENCY_BENEFICIAL);
				isBeneficial.add(OCCURRENCE_BENEFICIAL);

				expectedRule = topsis(mostLikelyRuleCandidates, weights, isBeneficial, preconditionSimList,
						ownershipList, frequencyList, occurrenceList);
				if (expectedRule == null) {
					// TODO TOPSIS Fehler abfangen
				}

			} else { // an error has happened
				// CASE ERROR HAPPENED
				// TODO expectedRule = letzte Rule, die mit device in question ausgelöst wurde
			}
		} else {
			// CASE NO EVENT HAPPENED

			// calculate Ownership
			ArrayList<Double> ownershipList = calculateOwnership(mostLikelyRuleCandidates, userId);

			// calculate frequency
			ArrayList<Double> frequencyList = calculateFrequency(mostLikelyRuleCandidates, FREQUENCY_THRESHOLD);

			// calculate occurrence
			ArrayList<Double> occurrenceList = calculateOccurrence(mostLikelyRuleCandidates, OCCURRENCE_THRESHOLD,
					userId);

			ArrayList<Double> weights = new ArrayList<Double>();
			weights.add(OWNERSHIP_WEIGHT);
			weights.add(FREQUENCY_WEIGHT);
			weights.add(OCCURRENCE_WEIGHT);

			ArrayList<Boolean> isBeneficial = new ArrayList<Boolean>();
			isBeneficial.add(OWNERSHIP_BENEFICIAL);
			isBeneficial.add(FREQUENCY_BENEFICIAL);
			isBeneficial.add(OCCURRENCE_BENEFICIAL);

			expectedRule = topsis(mostLikelyRuleCandidates, weights, isBeneficial, ownershipList, frequencyList,
					occurrenceList);
			if (expectedRule == null) {
				// TODO TOPSIS Fehler abfangen
			}
		}

		String pattern = transformationFunction(expectedRule, happenedEvent);

		explanation = callNLP(pattern);

		return explanation;
	}

	// is given List of Rules and explainee id, returns List with 1.0 if explainee
	// is owner and 0.0 if not owner
	private ArrayList<Double> calculateOwnership(ArrayList<Rule> ruleCandidates, String explaineeid) {
		ArrayList<Double> ownershipList = new ArrayList<Double>();
		for (Rule rule : ruleCandidates) {
			if (rule.getOwnerId().equals(explaineeid)) {
				ownershipList.add(1.0);
			} else {
				ownershipList.add(0.0);
			}
		}
		return ownershipList;
	}

	// is given a List of Rules and calculates how often they were called in the
	// last x days
	private ArrayList<Double> calculateFrequency(ArrayList<Rule> ruleCandidates, int thresholdDays) {

		// TODO discuss whether to outsource (e.g. to context Service)

		ArrayList<Double> frequencyList = new ArrayList<Double>();

		for (Rule candidate : ruleCandidates) {
			List<FrequencyEntry> entries = dataSer.findAllFrequencyEntriesByRuleId(candidate.getRuleId());
			int count = 0;
			LocalDateTime reference = LocalDateTime.now().minusDays(thresholdDays);
			ZoneId zone = ZoneId.systemDefault();

			for (FrequencyEntry entry : entries) {
				long timestampInMillis = entry.getTime();
				Instant instant = Instant.ofEpochMilli(timestampInMillis);
				LocalDateTime entryDateTime = LocalDateTime.ofInstant(instant, zone);
				if (entryDateTime.isAfter(reference)) {
					count++;
				}
			}
			frequencyList.add(Double.valueOf(count));
		}

		return frequencyList;
	}

	// is given a List of Rules and calculates how often they were explained to the
	// given user in the last x days
	private ArrayList<Double> calculateOccurrence(ArrayList<Rule> ruleCandidates, int thresholdDays,
			String explaineeId) {

		ArrayList<Double> occurrenceList = new ArrayList<Double>();

		for (Rule candidate : ruleCandidates) {
			occurrenceList.add(
					Double.valueOf(conSer.calculateOccurrenceCount(explaineeId, candidate.getRuleId(), thresholdDays)));
		}

		return occurrenceList;
	}

	private ArrayList<Double> calculatePreconditionSimilarity(ArrayList<Rule> ruleCandidates, Rule happenedRule) {

		ArrayList<Double> preconditionSimList = new ArrayList<Double>();

		ArrayList<LogEntry> happenedRulePrec = happenedRule.getTrigger();
		happenedRulePrec.addAll(happenedRule.getConditions());

		// create list of devices of preconditions of happened rule with possible duplicates
		ArrayList<String> happenedRuleDevicesWithDuplicates = new ArrayList<String>();
		for(LogEntry precondition : happenedRulePrec) {
			happenedRuleDevicesWithDuplicates.add(precondition.getEntityId());
		}
		ArrayList<String> happenedRuleDevices = removeDuplicates(happenedRuleDevicesWithDuplicates);
		
		for (Rule candidate : ruleCandidates) {
			ArrayList<LogEntry> candidatePrec = candidate.getTrigger();
			candidatePrec.addAll(candidate.getConditions());
			
			ArrayList<LogEntry> combP1 = new ArrayList<LogEntry>();
			combP1.addAll(candidatePrec);
			combP1.addAll(happenedRulePrec);
			ArrayList<LogEntry> combinedPrecList = removeDuplicates(combP1);
			
			// create list of devices of preconditions of candidate rule with possible duplicates
			ArrayList<String> candidateRuleDevicesWithDuplicates = new ArrayList<String>();
			for(LogEntry precondition : candidatePrec) {
				candidateRuleDevicesWithDuplicates.add(precondition.getEntityId());
			}
			ArrayList<String> candidateRuleDevices = removeDuplicates(happenedRuleDevicesWithDuplicates);
			
			ArrayList<String> combD1 = new ArrayList<String>();
			combD1.addAll(candidateRuleDevices);
			combD1.addAll(happenedRuleDevices);
			ArrayList<String> combinedDevices = removeDuplicates(combD1);

			int distanceBetweenCandidateAndHappenedRule = 0;
			for (LogEntry precon : combinedPrecList) {
				if (candidatePrec.contains(precon) ^ happenedRulePrec.contains(precon)) {
					distanceBetweenCandidateAndHappenedRule++;
				}
			}
			for (String device : combinedDevices) {
				if(candidateRuleDevices.contains(device) ^ happenedRuleDevices.contains(device)) {
					distanceBetweenCandidateAndHappenedRule++;
				}
			}
			// EXTEND WITH DEVICE PRECONDITIONS???

			preconditionSimList.add(1.0 - (distanceBetweenCandidateAndHappenedRule / (combinedPrecList.size() + combinedDevices.size())));
		}

		return preconditionSimList;
	}

	// can take not-normalized weights
	@SafeVarargs
	private Rule topsis(ArrayList<Rule> alternatives, ArrayList<Double> weights, ArrayList<Boolean> isBeneficial,
			ArrayList<Double>... matrixColumns) {
		// TODO iterate über alternatives ODER columns

		// check that lenghts of lists are same as dimensions of matrix
		if (weights.size() != alternatives.size() || isBeneficial.size() != matrixColumns.length) {
			return null;
		}

		for (ArrayList<Double> column : matrixColumns) {
			if (column.size() != alternatives.size()) {
				return null;
			}
		}

		// normalize weights
		ArrayList<Double> normalizedWeights = new ArrayList<Double>();
		Double weightsSum = weights.stream().mapToDouble(Double::doubleValue).sum();
		for (Double weight : weights) {
			normalizedWeights.add(weight / weightsSum);
		}

		// calculate normalized matrix from columns
		ArrayList<Double>[] normalizedMatrix = matrixColumns.clone();
		// TODO because of shallow object, change loops as described above
		for (ArrayList<Double> column : normalizedMatrix) {
			Double squaredSums = 0.0;
			for (Double value : column) {
				squaredSums += value * value;
			}
			Double rootOfSquaredSums = Math.sqrt(squaredSums);

			for (Double value : column) {
				value /= rootOfSquaredSums;
			}
		}

		// calculate weighted normalized matrix
		ArrayList<Double>[] weightedNormalizedMatrix = normalizedMatrix.clone();
		for (int i = 0; i < normalizedMatrix.length; i++) {
			for (int j = 0; j < normalizedMatrix[i].size(); j++) {
				normalizedMatrix[i].set(i, normalizedMatrix[i].get(j) * normalizedWeights.get(i));
			}
		}

		// calculate idealBestes/idealWorsts
		Double[] idealBests = new Double[normalizedWeights.size()];
		Double[] idealWorsts = new Double[normalizedWeights.size()];
		for (int i = 0; i < isBeneficial.size(); i++) {
			if (isBeneficial.get(i)) {
				// calculate column max for ideal Best and min for idealWorst
				idealBests[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).max()
						.orElseThrow(IllegalStateException::new);
				idealWorsts[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).min()
						.orElseThrow(IllegalStateException::new);
			} else {
				// calculate column min for ideal Best and max for idealWorst
				idealBests[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).min()
						.orElseThrow(IllegalStateException::new);
				idealWorsts[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).max()
						.orElseThrow(IllegalStateException::new);
			}
		}

		// calculate Euclidean distances from ideal best
		ArrayList<Double> s_iPlus = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			double totalDistancesSquared = 0.0;
			for (int j = 0; j < weightedNormalizedMatrix[i].size(); j++) {
				totalDistancesSquared += Math.pow((weightedNormalizedMatrix[i].get(j) - idealBests[j]), 2);
			}
			s_iPlus.add(Math.sqrt(totalDistancesSquared));
		}

		// calculate Euclidean distances from ideal best
		ArrayList<Double> s_iMinus = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			double totalDistancesSquared = 0.0;
			for (int j = 0; j < weightedNormalizedMatrix[i].size(); j++) {
				totalDistancesSquared += Math.pow((weightedNormalizedMatrix[i].get(j) - idealWorsts[j]), 2);
			}
			s_iMinus.add(Math.sqrt(totalDistancesSquared));
		}

		// calculate performance score
		ArrayList<Double> p_i = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			p_i.add(s_iMinus.get(i) / (s_iPlus.get(i) + s_iMinus.get(i)));
		}

		Double max = Collections.max(p_i);
		int index = p_i.indexOf(max);

		return alternatives.get(index);
	}

	private String transformationFunction(Rule expectedRule, Object happenedEvent) {
		// TODO construct pattern for explanation

		return null;
	}

	private String callNLP(String pattern) {
		// TODO call NLP API

		return null;
	}

	private <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
		
		Set<T> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;

		/*
		 * ArrayList<T> newList = new ArrayList<T>();
		 * 
		 * for (T element : list) { if (!newList.contains(element)) {
		 * newList.add(element); } } return newList;
		 */
	}

}
