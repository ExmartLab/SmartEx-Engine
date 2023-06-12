package exenginetest.loader;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import exengine.ExplainableEngineApplication;
import exengine.loader.JsonHandler;

@DisplayName("Unit Test Json Loading")
class JsonHandlerTest {
	
	@DisplayName("Test Loading LogEntry Objects From Json File")
	@Test
	void testLoadLogFile() throws IOException, URISyntaxException {
		// Given
		String fileName = "testingData/" + ExplainableEngineApplication.FILE_NAME_DEMO_LOGS;
		
		// When
		String json = JsonHandler.loadFile(fileName);
		
		// Then
		String expectedJson = "[\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:07:26.920189+00:00\",\n"
				+ "        \"name\": \"Deebot\",\n"
				+ "        \"state\": \"idle\",\n"
				+ "        \"entity_id\": \"vacuum.deebot\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:07:26.932243+00:00\",\n"
				+ "        \"name\": \"Deebot\",\n"
				+ "        \"state\": \"error\",\n"
				+ "        \"entity_id\": \"vacuum.deebot\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:07:26.933444+00:00\",\n"
				+ "        \"name\": \"Deebot last error\",\n"
				+ "        \"state\": \"104\",\n"
				+ "        \"entity_id\": \"sensor.deebot_last_error\",\n"
				+ "        \"icon\": \"mdi:alert-circle\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:50:50.014573+00:00\",\n"
				+ "        \"name\": \"state change\",\n"
				+ "        \"entity_id\": \"scene.state_change\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:50:50.229746+00:00\",\n"
				+ "        \"name\": \"Rule: Block High Coffee Consumption (sc1)\",\n"
				+ "        \"entity_id\": \"automation.test_scenario_watching_tv_light_off\",\n"
				+ "        \"message\": \"triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption\",\n"
				+ "        \"source\": \"state of sensor.smart_plug_social_room_coffee_today_s_consumption\",\n"
				+ "        \"context_id\": \"01G67ZHDBKS302M9XP2GJTZAJH\",\n"
				+ "        \"domain\": \"automation\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T09:50:50.848452+00:00\",\n"
				+ "        \"name\": \"Smart Plug Social Room Coffee\",\n"
				+ "        \"state\": \"off\",\n"
				+ "        \"entity_id\": \"switch.smart_plug_social_room_coffee\",\n"
				+ "        \"context_event_type\": \"automation_triggered\",\n"
				+ "        \"context_domain\": \"automation\",\n"
				+ "        \"context_name\": \"Rule: Block High Coffee Consumption (sc1)\",\n"
				+ "        \"context_message\": \"triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption\",\n"
				+ "        \"context_source\": \"state of sensor.smart_plug_social_room_coffee_today_s_consumption\",\n"
				+ "        \"context_entity_id\": \"automation.test_scenario_watching_tv_light_off\",\n"
				+ "        \"context_entity_id_name\": \"Rule: Block High Coffee Consumption (sc1)\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T11:19:30.181206+00:00\",\n"
				+ "        \"name\": \"Lab TV\",\n"
				+ "        \"state\": \"idle\",\n"
				+ "        \"entity_id\": \"media_player.lab_tv\",\n"
				+ "        \"context_event_type\": \"automation_triggered\",\n"
				+ "        \"context_domain\": \"automation\",\n"
				+ "        \"context_name\": \"Welcome\",\n"
				+ "        \"context_message\": \"triggered by state of binary_sensor.door\",\n"
				+ "        \"context_source\": \"state of binary_sensor.door\",\n"
				+ "        \"context_entity_id\": \"automation.presence_notification\",\n"
				+ "        \"context_entity_id_name\": \"Welcome\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T11:19:31.024951+00:00\",\n"
				+ "        \"name\": \"Lab TV\",\n"
				+ "        \"state\": \"playing\",\n"
				+ "        \"entity_id\": \"media_player.lab_tv\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T11:19:31.028089+00:00\",\n"
				+ "        \"name\": \"Rule: Block TV Audio During Meeting (sc2)\",\n"
				+ "        \"entity_id\": \"automation.sc2_multi_user_conflict\",\n"
				+ "        \"message\": \"triggered by state of media_player.lab_tv\",\n"
				+ "        \"source\": \"state of media_player.lab_tv\",\n"
				+ "        \"context_id\": \"01G684KSEJHD3DRWH9K36578E9\",\n"
				+ "        \"domain\": \"automation\",\n"
				+ "        \"context_state\": \"playing\",\n"
				+ "        \"context_entity_id\": \"media_player.lab_tv\",\n"
				+ "        \"context_entity_id_name\": \"Lab TV\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T11:19:31.037231+00:00\",\n"
				+ "        \"name\": \"tv_mute\",\n"
				+ "        \"entity_id\": \"scene.tv_playing\",\n"
				+ "        \"icon\": \"mdi:television\",\n"
				+ "        \"context_event_type\": \"automation_triggered\",\n"
				+ "        \"context_domain\": \"automation\",\n"
				+ "        \"context_name\": \"sc2: Multi-User-Conflict\",\n"
				+ "        \"context_message\": \"triggered by state of media_player.lab_tv\",\n"
				+ "        \"context_source\": \"state of media_player.lab_tv\",\n"
				+ "        \"context_entity_id\": \"automation.sc2_multi_user_conflict\",\n"
				+ "        \"context_entity_id_name\": \"Rule: Block TV Audio During Meeting (sc2)\"\n"
				+ "    },\n"
				+ "    {\n"
				+ "        \"when\": \"2022-06-23T11:19:31.126754+00:00\",\n"
				+ "        \"name\": \"tv_bright (constructed)\",\n"
				+ "        \"entity_id\": \"scene.tv_brightness\"\n"
				+ "    }\n"
				+ "]";
		Assertions.assertEquals(expectedJson, json);
	}

}
