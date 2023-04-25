package XLab.ExplainableEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import exengine.database.DatabaseService;
import exengine.datamodel.State;
import exengine.datamodel.User;
import exengine.rest.DatabaseController;

@Import(DatabaseController.class)
@WebMvcTest(DatabaseController.class)
public class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseService dataSer;

    @Configuration
    static class TestConfig {
        @Bean
        public DatabaseService databaseService() {
            return mock(DatabaseService.class);
        }
    }

    @Test
    public void testSetUserState() throws Exception {
        // mock data
        User user = new User();
        user.setName("John");
        user.setUserId("123");
        when(dataSer.findUserByUserId("123")).thenReturn(user);

        // send request
        mockMvc.perform(post("/database/state")
                .param("userid", "123")
                .param("userState", "working")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        		.andExpect(content().string("User John (id: 123) changed state to \"working\""))
                .andExpect(status().isOk());

        // verify data
        verify(dataSer).saveNewUser(user);
        assertThat(user.getState()).isEqualTo(State.WORKING);
    }
}
