package exengine.contextManager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.datamodel.*;
import exengine.database.*;

@Service
public class ContextService {

	@Autowired
	DatabaseService dataSer;

	public Context getAllContext(RuleCause cause, String explaineeId, State userState, String userLocation) {

		// get explainee and ruleOwner from database
		User explainee = dataSer.findUserByUserId(explaineeId);

		User ruleOwner = dataSer.findOwnerByRuleName(cause.getRule().getRuleName());
		// set dummy user in case rule has no owner (e.g. errors)
		if (ruleOwner == null)
			ruleOwner = new User("no owner", "0", Role.OWNER, Technicality.TECHNICAL);

		String ruleDescription = cause.getRule().getRuleDescription();

		Occurrence occurrence = dataSer.findOccurrence(explaineeId, cause.getRule().getRuleId(), 90);

		// adding the current occurrence
		dataSer.saveNewOccurrenceEntry(
				new OccurrenceEntry(explaineeId, cause.getRule().getRuleId(), new Date().getTime()));

		// check if explainee is Owner and set Role accordingly
		if (explainee.getId().equals(ruleOwner.getId()))
			explainee.setRole(Role.OWNER);

		Context context = new Context(explainee.getRole(), occurrence, explainee.getTechnicality(), userState,
				explainee.getName(), ruleOwner.getName(), ruleDescription);
		return context;
	}

}
