package exengine.contextAwareExpGenerator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;

import exengine.datamodel.*;
import exengine.expPresentation.ExplanationType;

@Service
public class ExplanationContextMappingService {

	private static final Logger logger = LoggerFactory.getLogger(ExplanationContextMappingService.class);

	public ExplanationType getExplanationType(Context c1, Cause cause) {

		if (cause.getClass().equals(RuleCause.class)) {

			// IMPORTANT: Parameter needs to be exact name of the package with the rulebook
			// classes
			RuleBookRunner ruleBookForRules = new RuleBookRunner("exengine.contextAwareExpGenerator.ruleBookForRules");
			NameValueReferableMap<Context> exfacts = new FactMap<>();

			// as a default, all explanation types are possible
			// IMPORTANT: The here assigned values are priorities which are in reverse order
			// to the expressiveness of the respective explanations
			List<Integer> exTypes = new ArrayList<Integer>();
			exTypes.add(1);
			exTypes.add(2);
			exTypes.add(3);
			exTypes.add(4);

			exfacts.put(new Fact<>(c1));

			// running the rulebook
			ruleBookForRules.setDefaultResult(exTypes);

			ruleBookForRules.run(exfacts);
			ruleBookForRules.getResult().ifPresent(result -> logger
					.debug("The explanation type is: {} (from the allowed: {})", c1.getTheExpType(), result));

			// getting the resulting type from the rulebook
			int type = c1.getTheExpType();

			// returning the resulting type as the respective enum constant
			if (type == ExplanationType.SIMPLDEX.getValue())
				return ExplanationType.SIMPLDEX;
			if (type == ExplanationType.RULEEX.getValue())
				return ExplanationType.RULEEX;
			if (type == ExplanationType.FACTEX.getValue())
				return ExplanationType.FACTEX;
			if (type == ExplanationType.FULLEX.getValue())
				return ExplanationType.FULLEX;

			return null;
		}

		else if (cause.getClass().equals(ErrorCause.class)) {

			// IMPORTANT: Parameter needs to be exact name of the package with the rulebook
			// classes
			RuleBookRunner ruleBook2 = new RuleBookRunner("exengine.contextAwareExpGenerator.ruleBookForErrors");
			NameValueReferableMap<Context> exfacts = new FactMap<>();

			// as a default, all explanation types are possible
			List<Integer> exTypes = new ArrayList<Integer>();
			exTypes.add(1);
			exTypes.add(2);
			exTypes.add(3);

			exfacts.put(new Fact<>(c1));

			// running the rulebook
			ruleBook2.setDefaultResult(exTypes);

			ruleBook2.run(exfacts);
			ruleBook2.getResult().ifPresent(result -> logger.debug("The explanation type is: {} (from the allowed: {})",
					c1.getTheExpType(), result));

			// getting the resulting type from the rulebook
			int type = c1.getTheExpType();

			// returning the resulting type as the respective enum constant
			if (type == ExplanationType.ERRFULLEX.getValue())
				return ExplanationType.ERRFULLEX;
			if (type == ExplanationType.ERRSOLEX.getValue())
				return ExplanationType.ERRSOLEX;
			if (type == ExplanationType.ERROREX.getValue())
				return ExplanationType.ERROREX;
			return null;

		}
		logger.debug("No valid explanation type found");
		return null;
	}

}
