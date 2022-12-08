package exengine.haconnection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import exengine.datamodel.LogEntry;

@Service
public class HomeAssistantConnectionService {

	//token needs to be a long lived home assistant bearer token (create one under http://IP_ADDRESS:8123/profile)
	String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIzMDlmODIyMzYxYTc0MDBmYmYxNTJhOTg2ZjU1MzlmMiIsImlhdCI6MTY0NjkwOTUwMiwiZXhwIjoxOTYyMjY5NTAyfQ.Fdov6W_HistZahjSurVQp4Tiln7UivGJR3JkqhXRSDk";
	//homeassistant.local can in URLs be replaced by 192.168.0.113
	String apiurl = "http://homeassistant.local:8123/api/";
	String logsurl = "http://homeassistant.local:8123/api/logbook/"; //without timecode
	static String explanationurl = "http://homeassistant.local:8123/api/states/sensor.virtual_explanation";
	String jsonsample = "[{\"when\": \"2022-04-07T13:40:19.738143+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:40:57.255034+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:07.102948+00:00\", \"name\": \"Worldclock Sensor\", \"state\": \"15:42\", \"entity_id\": \"sensor.worldclock_sensor\", \"icon\": \"mdi:clock\"}, {\"when\": \"2022-04-07T13:42:07.255856+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:17.580947+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}]";

	String s ="";
	
	public void printAPIStatus() {
		System.out.print("API status: ");
		String status = "not reachable";
		try {
			status = executeHttpClient(apiurl);
		} catch(Exception e) {
			status += " because " + e.getMessage();
		}
		System.out.println(status);
	}
	
	public ArrayList<LogEntry> parseLastLogs(int min) {
		return parseJSON(executeHttpClient(getURLlastXMin(min)));
	}
	
	public ArrayList<LogEntry> parseLogsLastHour() {
		return parseJSON(executeHttpClient(getURLlastHour()));
	}

	public String executeHttpClient(String url) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().setHeader("Authorization", "Bearer " + token).build();
		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {s = resp;}).join();
		return s;
		//client.sendAsync(request, BodyHandlers.ofFileDownload(Paths.get("C:\\Users\\PC\\Documents\\logs.txt")), null);
	}

	//testing main method
//	public static void main(String[] args) {
//		HomeAssistantConnectionService haSer = new HomeAssistantConnectionService();
//		haSer.postExplanation("test");
//	}
//	
	public String postExplanation(String state) {
		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"entity_id\": \"sensor.virtual_explanation\" ,\"state\": \"" + state + "\",\"attributes\": {\"friendly_name\":\"Explanation\",\"unique_id\":\"explanation\",\"device_class\": \"string\"}}";
		
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(explanationurl)).POST(HttpRequest.BodyPublishers.ofString(json)).setHeader("Authorization", "Bearer " + token).build();
		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {s = resp;}).join();
		return s;
	}
	
	public ArrayList<LogEntry> parseJSON(String json) {
		ArrayList<LogEntry> logsArrList = new ArrayList<LogEntry>();
		//System.out.println(json);
		//remove front and back brackets
		json = json.substring(1, json.length()-2);
		
		// Split String at curly brackets
		//List<String> logs = Arrays.asList(json.split("\\{"));
		List<String> logs = Arrays.asList(json.split("[{}]"));

		List<List<String>> loglist = new ArrayList<List<String>>();
		
		for(String s : logs) {
			//System.out.println(s);
			loglist.add(Arrays.asList(s.split("\\,")));	
		}
		
		for(List<String> list : loglist) {
			String time = null, name = null, state = null, entity_id = null;
			ArrayList<String> other = new ArrayList<String>();
			
			//cleaning front spaces
			for(int i = 0; i < list.size(); i++) {
				if(list.get(i).startsWith(" ")) {
					list.set(i, list.get(i).substring(1));
				}
			}
			
			//cases for LogEntry Attributes
			for(String s : list) {
				if(s.startsWith("\"when"))
					time = s.substring(9, s.length()-1);
				else if(s.startsWith("\"name"))
					name = s.substring(9, s.length()-1);
				else if(s.startsWith("\"state"))
					state = s.substring(10, s.length()-1);
				else if(s.startsWith("\"entity_id"))
					entity_id = s.substring(14, s.length()-1);
				else if(s.length() > 1)
					other.add(s);
			}
			if(time != null)
				logsArrList.add(new LogEntry(time, name, state, entity_id, other));
		}
		
		//System.out.println("\nlines: " + logs.size());
		//System.out.println("Logs: " + logsArrList.size());
		//System.out.println(loglist.size());
		return logsArrList;
	}
	
	public String getURLlastHour() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = logsurl + formatter.format(new Date())+"+03:00";
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		return s;
	}

	//returns URL for the last X minutes
	public String getURLlastXMin(int min) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		/*
		 * TODO Consider Daylight Saving Time
		 */
		int hours = min/60;
		min = min-(hours*60);
		String h = Integer.toString(2+hours);
		if(hours < 10)
			h = "0" + h;
		String m = Integer.toString(min);
		if(min < 10)
			m = "0" + m;
		String s = logsurl + formatter.format(new Date())+"+" + h + ":" + m;
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		//System.out.println(s);
		return s;
	}
	
	public String getURLentireDay() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String s = logsurl + formatter.format(new Date())+"T00:00:00+00:00";
		return s;
	}	

}
