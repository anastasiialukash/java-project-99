package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
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
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TaskStatus testTaskStatus;
    private User testUser;
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
        userRepository.save(testUser);

        // Create test task status
        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("Test Status");
        testTaskStatus.setSlug("test_status");
        testTaskStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    void tearDown() {
        taskStatusRepository.deleteAll();
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
    void testGetAllStatuses() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testTaskStatus.getId()))
                .andExpect(jsonPath("$[0].name").value(testTaskStatus.getName()))
                .andExpect(jsonPath("$[0].slug").value(testTaskStatus.getSlug()));
    }
    
    @Test
    void testGetStatusById() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", testTaskStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTaskStatus.getId()))
                .andExpect(jsonPath("$.name").value(testTaskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(testTaskStatus.getSlug()));
    }
    
    @Test
    void testCreateStatus() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        TaskStatusCreateDTO newStatus = new TaskStatusCreateDTO();
        newStatus.setName("New Status");
        newStatus.setSlug("new_status");
        
        mockMvc.perform(post("/api/task_statuses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(newStatus.getName()))
                .andExpect(jsonPath("$.slug").value(newStatus.getSlug()));
        
        List<TaskStatus> statuses = taskStatusRepository.findAll();
        assertThat(statuses).hasSize(2);
        assertThat(statuses.stream().anyMatch(s -> s.getSlug().equals(newStatus.getSlug()))).isTrue();
    }
    
    @Test
    void testCreateStatusWithoutAuthentication() throws Exception {
        TaskStatusCreateDTO newStatus = new TaskStatusCreateDTO();
        newStatus.setName("New Status");
        newStatus.setSlug("new_status");
        
        mockMvc.perform(post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isUnauthorized());

        int statusCount = taskStatusRepository.findAll().size();
        assertThat(taskStatusRepository.findBySlug("new_status").isEmpty()).isTrue();
    }
    
    @Test
    void testUpdateStatus() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        TaskStatusDTO updateStatus = new TaskStatusDTO();
        updateStatus.setName("Updated Status");
        updateStatus.setSlug("updated_status");
        
        mockMvc.perform(put("/api/task_statuses/{id}", testTaskStatus.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatus)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTaskStatus.getId()))
                .andExpect(jsonPath("$.name").value(updateStatus.getName()))
                .andExpect(jsonPath("$.slug").value(updateStatus.getSlug()));
        
        TaskStatus updatedStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo(updateStatus.getName());
        assertThat(updatedStatus.getSlug()).isEqualTo(updateStatus.getSlug());
    }
    
    @Test
    void testUpdateStatusWithoutAuthentication() throws Exception {
        TaskStatusDTO updateStatus = new TaskStatusDTO();
        updateStatus.setName("Updated Status");
        updateStatus.setSlug("updated_status");
        
        mockMvc.perform(put("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatus)))
                .andExpect(status().isUnauthorized());
        
        TaskStatus unchangedStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElseThrow();
        assertThat(unchangedStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(unchangedStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }
    
    @Test
    void testDeleteStatus() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isFalse();
    }
    
    @Test
    void testDeleteStatusWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/{id}", testTaskStatus.getId()))
                .andExpect(status().isUnauthorized());
        
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isTrue();
    }
    
    @Test
    void testGetStatusBySlug() throws Exception {
        mockMvc.perform(get("/api/task_statuses/slug/{slug}", testTaskStatus.getSlug()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTaskStatus.getId()))
                .andExpect(jsonPath("$.name").value(testTaskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(testTaskStatus.getSlug()));
    }
    
    @Test
    void testGetStatusByNonExistentSlug() throws Exception {
        mockMvc.perform(get("/api/task_statuses/slug/non_existent_slug"))
                .andExpect(status().isNotFound());
    }
}