package exengine.datamodel;

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
		String otherString = other == null ? "[]" : other.toString();
		return "time: " + time + " name: " + name + " state: " + state + " entity_id: " + entity_id + " other: " + otherString;  
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getEntity_id() {
		return entity_id;
	}

	public void setEntity_id(String entity_id) {
		this.entity_id = entity_id;
	}

	public ArrayList<String> getOther() {
		return other;
	}

	public void setOther(ArrayList<String> other) {
		this.other = other;
	}
	
}
