package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;
    private final String USER1_EMAIL = "user1@example.com";
    private final String USER2_EMAIL = "user2@example.com";
    private final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail(USER1_EMAIL);
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setPassword(passwordEncoder.encode(PASSWORD));
        user1.setCreatedAt(Instant.now());
        user1.setUpdatedAt(Instant.now());
        userRepository.save(user1);

        user2 = new User();
        user2.setEmail(USER2_EMAIL);
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setPassword(passwordEncoder.encode(PASSWORD));
        user2.setCreatedAt(Instant.now());
        user2.setUpdatedAt(Instant.now());
        userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String getToken(String username, String password) throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        return mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void testUserCanUpdateOwnAccount() throws Exception {
        String token = getToken(USER1_EMAIL, PASSWORD);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("UpdatedFirstName");

        mockMvc.perform(put("/api/users/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testUserCannotUpdateOtherUserAccount() throws Exception {
        String token = getToken(USER1_EMAIL, PASSWORD);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("UpdatedFirstName");

        mockMvc.perform(put("/api/users/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUserCanDeleteOwnAccount() throws Exception {
        String token = getToken(USER1_EMAIL, PASSWORD);

        mockMvc.perform(delete("/api/users/" + user1.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUserCannotDeleteOtherUserAccount() throws Exception {
        String token = getToken(USER1_EMAIL, PASSWORD);

        mockMvc.perform(delete("/api/users/" + user2.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUserCanCreateNewAccount() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("newuser@example.com");
        createDTO.setFirstName("New");
        createDTO.setLastName("User");
        createDTO.setPassword("newpassword");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
    }
}