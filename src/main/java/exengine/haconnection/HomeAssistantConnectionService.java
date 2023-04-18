package exengine.haconnection;

import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.LogEntry;
import exengine.loader.JsonHandler;

@Service
public class HomeAssistantConnectionService {

	// token needs to be a long lived home assistant bearer token (create one under
	// http://IP_ADDRESS:8123/profile)
	String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIzMDlmODIyMzYxYTc0MDBmYmYxNTJhOTg2ZjU1MzlmMiIsImlhdCI6MTY0NjkwOTUwMiwiZXhwIjoxOTYyMjY5NTAyfQ.Fdov6W_HistZahjSurVQp4Tiln7UivGJR3JkqhXRSDk";
	// homeassistant.local can in URLs be replaced by 192.168.0.113
	String apiurl = "http://homeassistant.local:8123/api/";
	String logsurl = "http://homeassistant.local:8123/api/logbook/"; // without timecode
	String explanationurl = apiurl + "states/sensor.virtual_explanation";
	String jsonsample = "[{\"when\": \"2022-04-07T13:40:19.738143+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:40:57.255034+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:07.102948+00:00\", \"name\": \"Worldclock Sensor\", \"state\": \"15:42\", \"entity_id\": \"sensor.worldclock_sensor\", \"icon\": \"mdi:clock\"}, {\"when\": \"2022-04-07T13:42:07.255856+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:17.580947+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}]";

	String s = "";

	public void printAPIStatus() {
		System.out.print("API status: ");
		String status = "not reachable";
		try {
			status = executeHttpClient(apiurl);
		} catch (Exception e) {
			status += " because " + e.getMessage();
		}
		System.out.println(status);
	}

	public ArrayList<LogEntry> parseLastLogs(int min) {
		return JsonHandler.parseJsonLog(executeHttpClient(getURLlastXMin(min)));
	}

	public ArrayList<LogEntry> parseLogsLastHour() {
		return JsonHandler.parseJsonLog(executeHttpClient(getURLlastHour()));
	}

	public String executeHttpClient(String url) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET()
				.setHeader("Authorization", "Bearer " + token).build();
		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {
			s = resp;
		}).join();
		return s;
		// client.sendAsync(request,
		// BodyHandlers.ofFileDownload(Paths.get("C:\\Users\\PC\\Documents\\logs.txt")),
		// null);
	}

	// testing main method
	public static void main(String[] args) {
		HomeAssistantConnectionService haSer = new HomeAssistantConnectionService();
		System.out.println(haSer.postExplanation("test"));
	}

	public String postExplanation(String state) {

		ObjectMapper mapper = new ObjectMapper();

		// create a JSON object
		ObjectNode expRes = mapper.createObjectNode();

		expRes.put("entity_id", "sensor.virtual_explanation");
		expRes.put("state",
				"Hi Alice,\\ntv_mute is active because Bob has set up a rule: \\\"Rule_2: mutes the TV if TV is playing while a meeting is going on\\\"\\nand currently a meeting in room 1 is going on and Lab TV is playing, so the rule has been fired.");
		expRes.put("entity_id", "sensor.virtual_explanation");

		// create a child JSON object
		ObjectNode attri = mapper.createObjectNode();
		attri.put("friendly_name", "Explanation");
		attri.put("unique_id", "explanation");
		attri.put("device_class", "string");

		// append address to expRes
		attri.set("attributes", attri);

//		String asString = mapper.writeValueAsString(expRes);
		
//		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"entity_id\": \"sensor.virtual_explanation\" ,\"state\": \"" + state
				+ "\", \"attributes\": { \"friendly_name\": \"Explanation\", \"unique_id\": \"explanation\", \"device_class\": \"string\"}}";
		String jso2 = "{\"entity_id\": \"sensor.virtual_explanation\", \"state\": \"" + state
				+ "\", \"attributes\": { \"friendly_name\": \"Explanation\", \"unique_id\": \"explanation\", \"device_class\": \"string\"}}";

		String payload = """
				{
    "entity_id": "sensor.virtual_explanation",
    "state": "Hi Alice",
    "attributes": {
        "friendly_name": "Explanation",
        "unique_id": "explanation",
        "device_class": "string"
    }
}
				""";
		
		String payload2 = """
				{
    "entity_id": "sensor.virtual_explanation",
    "state": "Hi Alice,\ntv_mute is active because Bob has set up a rule: \"Rule_2: mutes the TV if TV is playing while a meeting is going on\"\nand currently a meeting in room 1 is going on and Lab TV is playing, so the rule has been fired.",
    "attributes": {
        "friendly_name": "Explanation",
        "unique_id": "explanation",
        "device_class": "string"
    }
}
				""";

		String mockurl = "https://117fe9c3-e275-4f54-ae05-0571f16f3105.mock.pstmn.io";
		HttpClient client = HttpClient.newHttpClient();
		Builder builder = HttpRequest.newBuilder(URI.create(explanationurl)).POST(BodyPublishers.ofString(payload));
		try {
		builder = HttpRequest.newBuilder(URI.create(explanationurl)).POST(BodyPublishers.ofFile(Path.of("C:\\Users\\PC\\Desktop\\expTest.json")));
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		builder = builder.headers("Content-type", "application/json", "Authorization", "Bearer " + token);
		HttpRequest request = builder.build();
//		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {
//			s = resp;
//		}).join();
		HttpResponse<String> response = client.sendAsync(request, BodyHandlers.ofString()).join();

		System.out.println(payload.length());
		System.out.println(response.toString());
		System.out.println("body:\n" + response.body());
		System.out.println(response.headers().toString());
		
		
		s = "";
		return s;
	}

	public String getURLlastHour() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = logsurl + formatter.format(new Date()) + "+03:00";
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		return s;
	}

	// returns URL for the last X minutes
	public String getURLlastXMin(int min) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		/*
		 * TODO Consider Daylight Saving Time
		 */
		int hours = min / 60;
		min = min - (hours * 60);
		String h = Integer.toString(2 + hours);
		if (hours < 10)
			h = "0" + h;
		String m = Integer.toString(min);
		if (min < 10)
			m = "0" + m;
		String s = logsurl + formatter.format(new Date()) + "+" + h + ":" + m;
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		// System.out.println(s);
		return s;
	}

	public String getURLentireDay() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String s = logsurl + formatter.format(new Date()) + "T00:00:00+00:00";
		return s;
	}

}
