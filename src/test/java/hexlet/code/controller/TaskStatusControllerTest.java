package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private TaskRepository taskRepository;

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
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

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
                // Instead of checking specific array indices, verify that our test status exists in the array
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(testTaskStatus.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(org.hamcrest.Matchers.hasItem(testTaskStatus.getName())))
                .andExpect(jsonPath("$[*].slug").value(org.hamcrest.Matchers.hasItem(testTaskStatus.getSlug())));
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
        TaskStatusCreateDTO newStatus = new TaskStatusCreateDTO();
        newStatus.setName("New Status");
        newStatus.setSlug("new_status");
        
        mockMvc.perform(post("/api/task_statuses")
                .with(user(testUser.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(newStatus.getName()))
                .andExpect(jsonPath("$.slug").value(newStatus.getSlug()));
        
        List<TaskStatus> statuses = taskStatusRepository.findAll();
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
        TaskStatusDTO updateStatus = new TaskStatusDTO();
        updateStatus.setName("Updated Status");
        updateStatus.setSlug("updated_status");
        
        mockMvc.perform(put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(user(testUser.getEmail()))
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
        mockMvc.perform(delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(user(testUser.getEmail())))
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
        // Create a unique slug for this test to avoid duplicate data issues
        String uniqueSlug = "unique_test_slug_" + System.currentTimeMillis();
        testTaskStatus.setSlug(uniqueSlug);
        taskStatusRepository.save(testTaskStatus);
        
        mockMvc.perform(get("/api/task_statuses/slug/{slug}", uniqueSlug))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTaskStatus.getId()))
                .andExpect(jsonPath("$.name").value(testTaskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(uniqueSlug));
    }
    
    @Test
    void testGetStatusByNonExistentSlug() throws Exception {
        mockMvc.perform(get("/api/task_statuses/slug/non_existent_slug"))
                .andExpect(status().isNotFound());
    }
}