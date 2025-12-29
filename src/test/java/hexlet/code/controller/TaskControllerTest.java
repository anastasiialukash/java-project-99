package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;
    private Label testLabel;
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
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

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("Test Status");
        testTaskStatus.setSlug("test_status");
        testTaskStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(testTaskStatus);

        testLabel = new Label();
        testLabel.setName("Test Label");
        testLabel.setCreatedAt(Instant.now());
        labelRepository.save(testLabel);
        
        testTask = new Task();
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setCreatedAt(Instant.now());
        testTask.getLabels().add(testLabel);
        taskRepository.save(testTask);
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
    void testFilterTasksByTitleCont() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        Task secondTask = new Task();
        secondTask.setName("Another Task");
        secondTask.setDescription("Another Description");
        secondTask.setTaskStatus(testTaskStatus);
        secondTask.setAssignee(testUser);
        secondTask.setCreatedAt(Instant.now());
        taskRepository.save(secondTask);

        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Test")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()))
                .andExpect(jsonPath("$[0].title").value(testTask.getName()));
                
        // Filter by title containing "Another"
        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Another")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(secondTask.getId()))
                .andExpect(jsonPath("$[0].title").value(secondTask.getName()));
                
        // Filter by title containing "Task" (should return both)
        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Task")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void testFilterTasksByAssigneeId() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Create a second user
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");
        secondUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        secondUser.setCreatedAt(Instant.now());
        secondUser.setUpdatedAt(Instant.now());
        userRepository.save(secondUser);
        
        // Create a second task with a different assignee
        Task secondTask = new Task();
        secondTask.setName("Second Task");
        secondTask.setDescription("Second Description");
        secondTask.setTaskStatus(testTaskStatus);
        secondTask.setAssignee(secondUser);
        secondTask.setCreatedAt(Instant.now());
        taskRepository.save(secondTask);
        
        // Filter by first user's ID
        mockMvc.perform(get("/api/tasks")
                .param("assigneeId", testUser.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()));
                
        // Filter by second user's ID
        mockMvc.perform(get("/api/tasks")
                .param("assigneeId", secondUser.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(secondTask.getId()))
                .andExpect(jsonPath("$[0].assignee_id").value(secondUser.getId()));
    }
    
    @Test
    void testFilterTasksByStatus() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Create a second status
        TaskStatus secondStatus = new TaskStatus();
        secondStatus.setName("Second Status");
        secondStatus.setSlug("second_status");
        secondStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(secondStatus);
        
        // Create a second task with a different status
        Task secondTask = new Task();
        secondTask.setName("Second Task");
        secondTask.setDescription("Second Description");
        secondTask.setTaskStatus(secondStatus);
        secondTask.setAssignee(testUser);
        secondTask.setCreatedAt(Instant.now());
        taskRepository.save(secondTask);
        
        // Filter by first status
        mockMvc.perform(get("/api/tasks")
                .param("status", testTaskStatus.getSlug())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()))
                .andExpect(jsonPath("$[0].status").value(testTaskStatus.getSlug()));
                
        // Filter by second status
        mockMvc.perform(get("/api/tasks")
                .param("status", secondStatus.getSlug())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(secondTask.getId()))
                .andExpect(jsonPath("$[0].status").value(secondStatus.getSlug()));
    }
    
    @Test
    void testFilterTasksByLabelId() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Create a second label
        Label secondLabel = new Label();
        secondLabel.setName("Second Label");
        secondLabel.setCreatedAt(Instant.now());
        labelRepository.save(secondLabel);
        
        // Create a second task with a different label
        Task secondTask = new Task();
        secondTask.setName("Second Task");
        secondTask.setDescription("Second Description");
        secondTask.setTaskStatus(testTaskStatus);
        secondTask.setAssignee(testUser);
        secondTask.setCreatedAt(Instant.now());
        secondTask.getLabels().add(secondLabel);
        taskRepository.save(secondTask);
        
        // Filter by first label
        mockMvc.perform(get("/api/tasks")
                .param("labelId", testLabel.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()));
                
        // Filter by second label
        mockMvc.perform(get("/api/tasks")
                .param("labelId", secondLabel.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(secondTask.getId()));
    }
    
    @Test
    void testCombinedFilters() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        // Create a second status
        TaskStatus secondStatus = new TaskStatus();
        secondStatus.setName("Second Status");
        secondStatus.setSlug("second_status");
        secondStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(secondStatus);
        
        // Create a second label
        Label secondLabel = new Label();
        secondLabel.setName("Second Label");
        secondLabel.setCreatedAt(Instant.now());
        labelRepository.save(secondLabel);
        
        // Create a second task with different properties
        Task secondTask = new Task();
        secondTask.setName("Second Task");
        secondTask.setDescription("Second Description");
        secondTask.setTaskStatus(secondStatus);
        secondTask.setAssignee(testUser);
        secondTask.setCreatedAt(Instant.now());
        secondTask.getLabels().add(secondLabel);
        taskRepository.save(secondTask);
        
        // Filter by multiple criteria that match the first task
        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Test")
                .param("assigneeId", testUser.getId().toString())
                .param("status", testTaskStatus.getSlug())
                .param("labelId", testLabel.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()));
                
        // Filter by multiple criteria that match the second task
        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Second")
                .param("assigneeId", testUser.getId().toString())
                .param("status", secondStatus.getSlug())
                .param("labelId", secondLabel.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(secondTask.getId()));
                
        // Filter by criteria that don't match any task
        mockMvc.perform(get("/api/tasks")
                .param("titleCont", "Nonexistent")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}