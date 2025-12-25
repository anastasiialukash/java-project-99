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
    private ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testGetAllUsers() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(testUser.getLastName()))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void testGetUserById() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(get("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testCreateUser() throws Exception {
        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPassword("newpassword");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(newUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(newUser.getLastName()))
                .andExpect(jsonPath("$.password").doesNotExist());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users.stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()))).isTrue();
    }

    @Test
    void testUpdateUser() throws Exception {
        UserUpdateDTO updateUser = new UserUpdateDTO();
        updateUser.setEmail("updated@example.com");
        updateUser.setFirstName("Updated");

        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(updateUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(updateUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()))
                .andExpect(jsonPath("$.password").doesNotExist());
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo(updateUser.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateUser.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
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
}