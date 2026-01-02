package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private final String TEST_PASSWORD = "password";
    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
        userRepository.save(testUser);
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
    void testGetAllUsers() throws Exception {
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");
        secondUser.setPassword(passwordEncoder.encode("secondpassword"));
        secondUser.setCreatedAt(Instant.now());
        secondUser.setUpdatedAt(Instant.now());
        userRepository.save(secondUser);

        List<User> usersInDb = userRepository.findAll();

        List<UserDTO> usersFromDb = usersInDb.stream()
                .map(userMapper::map)
                .toList();

        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        String responseJson = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<UserDTO> usersFromResponse =
                objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(usersFromResponse)
                .containsExactlyInAnyOrderElementsOf(usersFromDb);
    }

    @Test
    void testGetUserById() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        MvcResult result = mockMvc.perform(get("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        UserDTO actual = objectMapper.readValue(json, UserDTO.class);
        UserDTO expected = userMapper.map(testUser);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testCreateUser() throws Exception {
        String email = "new@example.com";
        String firstName = "New";
        String lastName = "User";

        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword("newpassword");

        UserDTO expectedUser = new UserDTO();
        expectedUser.setEmail(email);
        expectedUser.setFirstName(firstName);
        expectedUser.setLastName(lastName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        String responseJson = mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDTO userFromResponse = objectMapper.readValue(responseJson, UserDTO.class);

        expectedUser.setCreatedAt(userFromResponse.getCreatedAt());
        expectedUser.setId(userFromResponse.getId());

        assertThat(userFromResponse).isEqualTo(expectedUser);
    }

    @Test
    void testUpdateUser() throws Exception {
        UserUpdateDTO updateUser = new UserUpdateDTO();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");

        UserDTO expectedUser = new UserDTO();
        expectedUser.setId(testUser.getId());
        expectedUser.setFirstName(updateUser.getFirstName());
        expectedUser.setLastName(testUser.getLastName());
        expectedUser.setEmail(updateUser.getEmail());

        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        String response = mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDTO userFromResponse = objectMapper.readValue(response, UserDTO.class);
        expectedUser.setCreatedAt(userFromResponse.getCreatedAt());
        assertThat(userFromResponse).isEqualTo(expectedUser);
    }

    @Test
    void testDeleteUser() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    void testCreateUserWithInvalidData() throws Exception {
        UserCreateDTO invalidUser = new UserCreateDTO();
        invalidUser.setEmail("invalid-email");
        invalidUser.setFirstName("New");
        invalidUser.setLastName("User");
        invalidUser.setPassword("pw");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser))).andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void testUpdateUserWithInvalidData() throws Exception {
        UserUpdateDTO invalidUpdate = new UserUpdateDTO();
        invalidUpdate.setEmail("invalid-email");
        
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate))).andExpect(status().isBadRequest());

        User unchangedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void testGetNonExistentUser() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(get("/api/users/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateNonExistentUser() throws Exception {
        UserUpdateDTO updateUser = new UserUpdateDTO();
        updateUser.setEmail("updated@example.com");
        
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(put("/api/users/999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser))).andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistentUser() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(delete("/api/users/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testUpdateAnotherUserReturns403() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPassword(passwordEncoder.encode("anotherpassword"));
        anotherUser.setCreatedAt(Instant.now());
        anotherUser.setUpdatedAt(Instant.now());
        userRepository.save(anotherUser);
        
        // Get token for the first user
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Try to update the second user with the first user's token
        UserUpdateDTO updateUser = new UserUpdateDTO();
        updateUser.setFirstName("Hacked");
        
        mockMvc.perform(put("/api/users/{id}", anotherUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isForbidden());
        
        // Verify the user was not updated
        User unchangedUser = userRepository.findById(anotherUser.getId()).orElseThrow();
        assertThat(unchangedUser.getFirstName()).isEqualTo(anotherUser.getFirstName());
    }
    
    @Test
    void testDeleteAnotherUserReturns403() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPassword(passwordEncoder.encode("anotherpassword"));
        anotherUser.setCreatedAt(Instant.now());
        anotherUser.setUpdatedAt(Instant.now());
        userRepository.save(anotherUser);
        
        // Get token for the first user
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Try to delete the second user with the first user's token
        mockMvc.perform(delete("/api/users/{id}", anotherUser.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        
        // Verify the user was not deleted
        assertThat(userRepository.existsById(anotherUser.getId())).isTrue();
    }
}