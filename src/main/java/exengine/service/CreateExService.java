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

	@Autowired
	private RuleRepository ruleRepo;
	
	public List<Rule> findRules() {
		return ruleRepo.findAll();
	}
	
	public List<Rule> findRulesByName() {
		return ruleRepo.findByRuleName("testRule1");
	}
	
	public String getExplanation(int min) {
		ArrayList<LogEntry> logEntries = HA_API.parseLastLogs(min);
		String explanation = "";
		for(LogEntry l : logEntries)
			System.out.println(l.toString());
		
		//TODO initialize lists for actions and rules
		
		
		//iterate through Log Entries in reversed order
		for(int i = logEntries.size()-1; i>=0; i--) {
//			if(logEntries.get(i).getName())
		}
		/*
		 * TODO
		 * createExpl algorithm
		 */
		
		
		return explanation;
	}
	
}