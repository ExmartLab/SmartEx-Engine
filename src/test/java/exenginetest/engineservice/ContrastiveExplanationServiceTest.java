package exenginetest.engineservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import exengine.database.DatabaseService;
import exengine.engineservice.ContrastiveExplanationService;

@DisplayName("Unit Test ContrastiveExplanationService")
@ExtendWith(MockitoExtension.class)
public class ContrastiveExplanationServiceTest {

	@InjectMocks
	private ContrastiveExplanationService underTest;

	@Mock
	private DatabaseService dataSer;

	//TODO write tests for ContrastiveExplanationService
	
}
