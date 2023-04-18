package exengine.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import exengine.datamodel.LogEntry;

public class JsonHandler {
	
	public static String loadJsonFileAsString(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }
	
	public static ArrayList<LogEntry> parseJsonLog(String json) {
		ArrayList<LogEntry> logsArrList = new ArrayList<LogEntry>();
		// System.out.println(json);
		// remove front and back brackets
		json = json.substring(1, json.length() - 2);

		// Split String at curly brackets
		// List<String> logs = Arrays.asList(json.split("\\{"));
		List<String> logs = Arrays.asList(json.split("[{}]"));

		List<List<String>> loglist = new ArrayList<List<String>>();

		for (String s : logs) {
			// System.out.println(s);
			loglist.add(Arrays.asList(s.split("\\,")));
		}

		for (List<String> list : loglist) {
			String time = null, name = null, state = null, entity_id = null;
			ArrayList<String> other = new ArrayList<String>();

			// cleaning front spaces
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).startsWith(" ")) {
					list.set(i, list.get(i).substring(1));
				}
			}

			// cases for LogEntry Attributes
			for (String s : list) {
				if (s.startsWith("\"when"))
					time = s.substring(9, s.length() - 1);
				else if (s.startsWith("\"name"))
					name = s.substring(9, s.length() - 1);
				else if (s.startsWith("\"state"))
					state = s.substring(10, s.length() - 1);
				else if (s.startsWith("\"entity_id"))
					entity_id = s.substring(14, s.length() - 1);
				else if (s.length() > 1)
					other.add(s);
			}
			if (time != null)
				logsArrList.add(new LogEntry(time, name, state, entity_id, other));
		}

		// System.out.println("\nlines: " + logs.size());
		// System.out.println("Logs: " + logsArrList.size());
		// System.out.println(loglist.size());
		return logsArrList;
	}

}
