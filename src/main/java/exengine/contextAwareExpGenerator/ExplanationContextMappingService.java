package exengine.contextAwareExpGenerator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.Context;
import exengine.expPresentation.ExplanationType;

@Service
public class ExplanationContextMappingService {

	public ExplanationType getExplanationType(Context c1) {

		// IMPORTANT: Parameter needs to be exact name of the package with the rulebook
		// classes
		RuleBookRunner ruleBook2 = new RuleBookRunner("exengine.contextAwareExpGenerator");
		NameValueReferableMap<Context> exfacts = new FactMap<>();

		// as a default, all explanation types are possible
		List<Integer> exTypes = new ArrayList<Integer>();
		exTypes.add(1);
		exTypes.add(2);
		exTypes.add(3);
		exTypes.add(4);

		exfacts.put(new Fact<>(c1));

		//running the rulebook
		ruleBook2.setDefaultResult(exTypes);

		ruleBook2.run(exfacts);
		ruleBook2.getResult().ifPresent(result -> System.out.println("Final allowed Expalnation Types are: " + result));

		if (ExplainableEngineApplication.debug)
			System.out.println("So the explanation type to generate, would be: " + c1.getTheExpType());

		//getting the resulting type from the rulebook
		int type = c1.getTheExpType();

		//returning the resulting type as the respective enum constant
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

}
