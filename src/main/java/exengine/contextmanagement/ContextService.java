package exengine.contextmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.datamodel.*;
import exengine.database.*;
import exengine.explanationgenerationservice.Cause;

@Service
public class ContextService {

	@Autowired
	DatabaseService dataSer;

	public Context getAllContext(Cause cause, int explaineeId, State userState, String userLocation) {

		// get explainee and ruleOwner from database
		User explainee = dataSer.findUserByUserId(explaineeId);
		User ruleOwner = dataSer.findOwnerByRuleName(cause.rule);

		// get occurrence from db
		Occurrence occurrence = null;

		Role explaineeRole = explainee.getRole();
		Technicality explaineeTechnicality = explainee.getTechnicality();

		State explaineeState = userState;

		Context context = new Context(explaineeRole, occurrence, explaineeTechnicality, explaineeState, null);
		context.setExplaineeName(explainee.getName());
		context.setOwnerName(ruleOwner.getName());
		/*
		 * TODO new constructor for context (without exptype and with explainee - / owner name
		 */

		return context;
	}


}
