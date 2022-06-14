package exengine.haconnection;

import java.util.ArrayList;

public class LogEntry {

	public String time, name, state, entity_id;
	public ArrayList<String> other;
	
	public LogEntry(String time, String name, String state, String entity_id, ArrayList<String> other) {
		this.time = time;
		this.name = name;
		this.state = state;
		this.entity_id = entity_id;
		this.other = other;
	}
	
	public String toString() {
		return "time: " + time + " name: " + name + " state: " + state + " entity_id: " + entity_id + " other: " + other.toString();  
	}
	
	
	
	
	
}
