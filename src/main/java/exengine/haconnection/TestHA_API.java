package exengine.haconnection;

import java.util.ArrayList;

public class TestHA_API {
	
	public static void main(String[] args) {
		ArrayList<LogEntry> logEntries = HA_API.parseLastLogs(20);
		System.out.println(logEntries.size());
		for(LogEntry l : logEntries) {
			System.out.println(l.toString());
		}
	}
}
