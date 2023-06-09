package exenginetest.haconnection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import exengine.haconnection.HomeAssistantConnectionService;

@DisplayName("Unit Test Home Assistant Connection")
class HomeAssistantConnectionServiceTest {
	private HomeAssistantConnectionService underTest;
	
	@BeforeEach
	void init() {
		underTest = new HomeAssistantConnectionService();
	}
	
	@Disabled
	@Test
	void testGetURLlastXMin() {
		// Given
		int min = 30;
		
		// When
		String urlNew = underTest.getURL(min);
		String urlOld = "http://homeassistant.local:8123/api/logbook/2023-06-05T15:40:15+02:30";
		
		// Then
		System.out.println("actual:  " + urlNew);
		System.out.println("expected: " + urlOld);
		
		Assertions.assertEquals(urlOld, urlNew);
	}

}
