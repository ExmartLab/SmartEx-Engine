package exengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.database.Path;
import exengine.database.PathRepository;
import exengine.haconnection.LogEntry;

@Service
public class CreateExService {

	@Autowired
	private PathRepository repository;
	
	public List<Path> findPaths() {
		return repository.findAll();
	}
	
	public List<Path> findPathsByRule() {
		return repository.findByRuleName("testRule1");
	}
	
	public String getExplanation(ArrayList<LogEntry> logEntries) {
		String explanation = "";
		/*
		 * TODO
		 * createExpl algorithm
		 */
		
		
		return explanation;
	}
	
}
