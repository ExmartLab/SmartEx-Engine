package exengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.database.Rule;
import exengine.database.RuleRepository;
import exengine.haconnection.HA_API;
import exengine.haconnection.LogEntry;

@Service
public class CreateExService {

	private List<Rule> dbRules;
	
	@Autowired
	private RuleRepository ruleRepo;
	
	public String getExplanation(int min) {
		ArrayList<LogEntry> logEntries = HA_API.parseLastLogs(min);
		String explanation = "";
		for(LogEntry l : logEntries)
			System.out.println(l.toString());
		
		//initialize lists for actions and rules from Logs
		ArrayList<String> foundActions = new ArrayList<String>();
		ArrayList<String> foundRules = new ArrayList<String>();

		//query Rules from DB
		dbRules = findRules();
		
		//iterate through Log Entries in reversed order
		for(int i = logEntries.size()-1; i>=0; i--) {
			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();
			if(isInActions(entryData)) {
				foundActions.add(entryData);
			}
			else if(isInRules(entryData)) {
				foundRules.add(entryData);
			}
		}
		/*
		 * TODO
		 * createExpl algorithm
		 */
		
		
		return explanation;
	}
	
	public boolean isInActions(String toCheck) {
		boolean result = false;
		for(Rule r : dbRules) {
			if(r.getAction().equals(toCheck)) {
				result = true;
			}
		}
		return result;
	}
	
	public boolean isInRules(String toCheck) {
		boolean result = false;
		for(Rule r : dbRules) {
			if(r.getRuleName().equals(toCheck)) {
				result = true;
			}
		}
		return result;
	}
	
	public List<Rule> findRules() {
		return ruleRepo.findAll();
	}
	
	/*
	public List<Rule> findRulesByName() {
		return ruleRepo.findByRuleName("testRule1");
	}
	*/
	
	
}