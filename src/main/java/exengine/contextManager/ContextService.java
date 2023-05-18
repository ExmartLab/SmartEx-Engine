package exengine.contextManager;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.datamodel.*;
import exengine.database.*;

@Service
public class ContextService {
	
	private static final Logger logger = LoggerFactory.getLogger(ContextService.class);

	@Autowired
	DatabaseService dataSer;

	public Context getAllContext(Cause cause, String explaineeId, State userState, String userLocation) {

		// initialize return value with null as default
		Context context = null;
		Occurrence occurrence = null;
		String id = null;

		// get explainee and ruleOwner from database
		User explainee = dataSer.findUserByUserId(explaineeId);

		// Rule case
		if (cause.getClass().equals(RuleCause.class)) {
			
			id = ((RuleCause) cause).getRule().getRuleId();

			occurrence = dataSer.findOccurrence(explaineeId, id, 90);

			User ruleOwner = dataSer.findOwnerByRuleName(((RuleCause) cause).getRule().getRuleName());
			// set default user in case rule has no owner
			if (ruleOwner == null)
				ruleOwner = new User("no owner", "0", Role.OWNER, Technicality.TECHNICAL);

			// check if explainee is Owner and set Role accordingly
			if (explainee.getId().equals(ruleOwner.getId()))
				explainee.setRole(Role.OWNER);

			context = new Context(explainee.getRole(), occurrence, explainee.getTechnicality(), userState,
					explainee.getName(), ruleOwner.getName());
		}
		// Error case
		else if (cause.getClass().equals(ErrorCause.class)) {
			
			id = ((ErrorCause) cause).getError().getErrorId();
					
			occurrence = dataSer.findOccurrence(explaineeId, id, 90);
			context = new Context(explainee.getRole(), occurrence, explainee.getTechnicality(), userState,
					explainee.getName(), null);
		}
		// adding the current occurrence
		dataSer.saveNewOccurrenceEntry(
		new OccurrenceEntry(explaineeId, id, new Date().getTime()));

		logger.debug("Found context contains owner: {}, explainee: {}, role: {}, state: {}, technicality: {}, occurrence: {}, device: {}",
				context.getOwnerName(),
				context.getExplaineeName(),
				context.getExplaineeRole().toString(),
				context.getExplaineeState().toString(),
				context.getExplaineeTechnicality().toString(),
				context.getOccurrence());
		
		return context;
	}

}
