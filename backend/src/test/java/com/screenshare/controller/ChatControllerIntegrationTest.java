package com.screenshare.controller;

import com.screenshare.entity.User;
import com.screenshare.repository.ChatRoomRepository;
import com.screenshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private User alice;

    @BeforeEach
    public void setup() {
        // Rely on create-drop to provide a fresh schema; insert minimal test user
        alice = new User("alice2", "alice2@example.com", "password");
        userRepository.save(alice);
    }

    @Test
    public void createRoom_endpoint_createsPrivateRoom() throws Exception {
        String body = "{\"name\":\"API Private\",\"description\":\"api desc\",\"isPrivate\":true}";

        mockMvc.perform(post("/chat/rooms?creatorId=" + alice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("PRIVATE"))
                .andExpect(jsonPath("$.description").value("api desc"));
    }
}
