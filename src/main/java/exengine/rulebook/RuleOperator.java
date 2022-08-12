package exengine.rulebook;

import java.util.ArrayList;
import java.util.List;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;

import exengine.datamodel.Context;
import exengine.datamodel.Occurrence;
import exengine.datamodel.Role;
import exengine.datamodel.State;
import exengine.datamodel.Technicality;
import exengine.explanationtypes.ExplanationType;

public class RuleOperator {
	
	/*
	public static <T> void main(String[] args) {

	    RuleBookRunner ruleBook2 = new RuleBookRunner("explainRule");
	    NameValueReferableMap<Context> exfacts = new FactMap<>();
	    List<Integer> exTypes= new ArrayList<Integer>();
	    exTypes.add(1);
	    exTypes.add(2);
	    exTypes.add(3);
	    exTypes.add(4);
	    

	    Context c1 = new Context("Meeting","Owner","M", "H",null);
	   
	    exfacts.put(new Fact<>(c1));
	
	   
	    
	    ruleBook2.setDefaultResult(exTypes);
	    
	    ruleBook2.run(exfacts);
	    ruleBook2.getResult().ifPresent(result -> System.out.println("Final allowed Expalnation Types are: " + result));
	  
	    System.out.println("So the explanation type to generate, would be: " + c1.getTheExpType());
	    
	    	
	  }
*/	


	public static ExplanationType getExplanationType(Context c1) {
		System.out.println("DEBUG: getType");
	    RuleBookRunner ruleBook2 = new RuleBookRunner("explainRule");
	    NameValueReferableMap<Context> exfacts = new FactMap<>();
	    List<Integer> exTypes= new ArrayList<Integer>();
	    exTypes.add(1);
	    exTypes.add(2);
	    exTypes.add(3);
	    exTypes.add(4);
	    

//	    Context c1 = new Context("Meeting","Owner","M", "H",null);
	    
	    exfacts.put(new Fact<>(c1));	   
	    
	    ruleBook2.setDefaultResult(exTypes);
	    
	    ruleBook2.run(exfacts);
	    ruleBook2.getResult().ifPresent(result -> System.out.println("Final allowed Expalnation Types are: " + result));
	    
	    System.out.println("So the explanation type to generate, would be: " + c1.getTheExpType());
	    
	    switch(c1.getTheExpType()) {
	    case 1:
	    	return ExplanationType.SIMPLYDEX;
	    case 2:
	    	return ExplanationType.FACTEX;
	    case 3:
	    	return ExplanationType.RULEEX;
	    case 4:
	    	return ExplanationType.FULLEX;
	    }
	    return null;
	}

}
