package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;
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

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("Test Status");
        testTaskStatus.setSlug("test_status");
        testTaskStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(testTaskStatus);

        testTask = new Task();
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setCreatedAt(Instant.now());
        taskRepository.save(testTask);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
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
    void testGetAllTasks() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testTask.getId()))
                .andExpect(jsonPath("$[0].title").value(testTask.getName()))
                .andExpect(jsonPath("$[0].content").value(testTask.getDescription()))
                .andExpect(jsonPath("$[0].status").value(testTaskStatus.getSlug()))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()));
    }

    @Test
    void testGetTaskById() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value(testTask.getName()))
                .andExpect(jsonPath("$.content").value(testTask.getDescription()))
                .andExpect(jsonPath("$.status").value(testTaskStatus.getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));
    }

    @Test
    void testCreateTask() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        TaskCreateDTO newTask = new TaskCreateDTO();
        newTask.setTitle("New Task");
        newTask.setContent("New Description");
        newTask.setStatus(testTaskStatus.getSlug());
        newTask.setAssignee_id(testUser.getId());

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(newTask.getTitle()))
                .andExpect(jsonPath("$.content").value(newTask.getContent()))
                .andExpect(jsonPath("$.status").value(newTask.getStatus()))
                .andExpect(jsonPath("$.assignee_id").value(newTask.getAssignee_id()));

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(2);
        assertThat(tasks.stream().anyMatch(t -> t.getName().equals(newTask.getTitle()))).isTrue();
    }

    @Test
    void testUpdateTask() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        TaskUpdateDTO updateTask = new TaskUpdateDTO();
        updateTask.setTitle("Updated Task");
        updateTask.setContent("Updated Description");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value(updateTask.getTitle()))
                .andExpect(jsonPath("$.content").value(updateTask.getContent()))
                .andExpect(jsonPath("$.status").value(testTaskStatus.getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo(updateTask.getTitle());
        assertThat(updatedTask.getDescription()).isEqualTo(updateTask.getContent());
    }

    @Test
    void testDeleteTask() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isFalse();
    }

    @Test
    void testAccessTasksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isUnauthorized());

        TaskCreateDTO newTask = new TaskCreateDTO();
        newTask.setTitle("New Task");
        newTask.setContent("New Description");
        newTask.setStatus(testTaskStatus.getSlug());

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isUnauthorized());

        TaskUpdateDTO updateTask = new TaskUpdateDTO();
        updateTask.setTitle("Updated Task");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isUnauthorized());
    }
}