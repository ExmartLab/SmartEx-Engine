package exengine.rulebook;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;

import exengine.datamodel.Context;
import exengine.explanationtypes.ExplanationType;

@Service
public class ExplanationTypeService {

	public ExplanationType getExplanationType(Context c1) {
		RuleBookRunner ruleBook2 = new RuleBookRunner("exengine.explainTypeRuleEngine");
		NameValueReferableMap<Context> exfacts = new FactMap<>();
		List<Integer> exTypes = new ArrayList<Integer>();
		exTypes.add(1);
		exTypes.add(2);
		exTypes.add(3);
		exTypes.add(4);

		exfacts.put(new Fact<>(c1));

		ruleBook2.setDefaultResult(exTypes);

		ruleBook2.run(exfacts);
		ruleBook2.getResult().ifPresent(result -> System.out.println("Final allowed Expalnation Types are: " + result));

		System.out.println("So the explanation type to generate, would be: " + c1.getTheExpType());

		switch (c1.getTheExpType()) {
		case 1:
			return ExplanationType.SIMPLDEX;
		case 2:
			return ExplanationType.FACTEX;
		case 3:
			return ExplanationType.RULEEX;
		case 4:
			return ExplanationType.FULLEX;
		}
		return null;
	}

	/*
	 * public static <T> void main(String[] args) {
	 * 
	 * RuleBookRunner ruleBook2 = new RuleBookRunner("explainRule");
	 * NameValueReferableMap<Context> exfacts = new FactMap<>(); List<Integer>
	 * exTypes= new ArrayList<Integer>(); exTypes.add(1); exTypes.add(2);
	 * exTypes.add(3); exTypes.add(4);
	 * 
	 * 
	 * Context c1 = new Context("Meeting","Owner","M", "H",null);
	 * 
	 * exfacts.put(new Fact<>(c1));
	 * 
	 * 
	 * 
	 * ruleBook2.setDefaultResult(exTypes);
	 * 
	 * ruleBook2.run(exfacts); ruleBook2.getResult().ifPresent(result ->
	 * System.out.println("Final allowed Expalnation Types are: " + result));
	 * 
	 * System.out.println("So the explanation type to generate, would be: " +
	 * c1.getTheExpType());
	 * 
	 * 
	 * }
	 */

}
