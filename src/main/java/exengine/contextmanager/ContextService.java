package exengine.contextmanager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.database.DatabaseService;
import exengine.datamodel.Context;
import exengine.datamodel.Error;
import exengine.datamodel.Occurrence;
import exengine.datamodel.OccurrenceEntry;
import exengine.datamodel.Role;
import exengine.datamodel.Rule;
import exengine.datamodel.User;
import exengine.datamodel.State;
import exengine.datamodel.Technicality;

/**
 * Component to collect all contextual variables for enriching explanations.
 */
@Service
public class ContextService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextService.class);

	@Autowired
	DatabaseService dataSer;

	/**
	 * Bundles contextual variables into a Context object.
	 * 
	 * @param cause     causal path as determined by e.g., the FindCauseService
	 * @param explainee User that requests an explanation
	 * @return Context (bundle of all contextual variables)
	 */
	public Context getAllContext(Object cause, User explainee) {

		String explaineeId = explainee.getUserId();
		State state = explainee.getState();
		Context context = null;
		Occurrence occurrence = null;
		String id = null;

		// Rule case
		if (cause instanceof Rule rule) {

			id = rule.getRuleId();

			occurrence = findOccurrence(explaineeId, id, 90);

			User ruleOwner = dataSer.findOwnerByRuleName(rule.getRuleName());
			// Set default user in case rule has no owner
			if (ruleOwner == null) {
				ruleOwner = new User("no owner", "0", Role.OWNER, Technicality.TECHNICAL);
			}

			// Check if explainee is Owner and set Role accordingly
			if (explainee.getId().equals(ruleOwner.getId())) {
				explainee.setRole(Role.OWNER);
			}

			context = new Context(explainee.getRole(), occurrence, explainee.getTechnicality(), state,
					explainee.getName(), ruleOwner.getName());
		}
		// Error case
		else if (cause instanceof Error error) {

			id = error.getErrorId();

			occurrence = findOccurrence(explaineeId, id, 90);
			context = new Context(explainee.getRole(), occurrence, explainee.getTechnicality(), state,
					explainee.getName(), null);
		}

		// Adding the current occurrence
		dataSer.saveNewOccurrenceEntry(new OccurrenceEntry(explaineeId, id, new Date().getTime()));

		if (context != null) {
			LOGGER.debug(
					"Found context contains owner: {}, explainee: {}, role: {}, state: {}, technicality: {}, occurrence: {}",
					context.getOwnerName(), context.getExplaineeName(), context.getExplaineeRole(),
					context.getExplaineeState(), context.getExplaineeTechnicality(), context.getOccurrence());
		}

		return context;
	}

	/**
	 * Finds an occurrence by user ID, rule ID. This method counts the observations 
	 *
	 * @param userId the user ID
	 * @param ruleId the rule ID
	 * @param days   the number of days
	 * @return the occurrence (basically the count encoded in the Occurrence enum)
	 */
	public Occurrence findOccurrence(String userId, String ruleId, int days) {
		ArrayList<OccurrenceEntry> entries = dataSer.findOccurrenceEntriesByUserIdAndRuleId(userId, ruleId);
		int count = 0;
		LocalDateTime reference = LocalDateTime.now().minusDays(days);
		ZoneId zone = ZoneId.systemDefault();

		for (OccurrenceEntry entry : entries) {
			long timestampInMillis = entry.getTime();
			Instant instant = Instant.ofEpochMilli(timestampInMillis);
			LocalDateTime entryDateTime = LocalDateTime.ofInstant(instant, zone);
			if (entryDateTime.isAfter(reference)) {
				count++;
			}
			if (count > 2) { // stop early because count > 2 are treated uniformly
				break;
			}
		}

		switch (count) {
		case 0:
			return Occurrence.FIRST;
		case 1:
			return Occurrence.SECOND;
		default:
			return Occurrence.MORE;
		}
	}

}
