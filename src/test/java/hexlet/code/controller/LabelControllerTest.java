package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LoginRequestDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Label testLabel;
    private static final String TEST_USERNAME = "testuser@example.com";
    private static final String TEST_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail(TEST_USERNAME);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setCreatedAt(Instant.now());
        userRepository.save(testUser);

        testLabel = new Label();
        testLabel.setName("Test Label");
        testLabel.setCreatedAt(Instant.now());
        labelRepository.save(testLabel);
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
    void testGetAllLabels() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(get("/api/labels")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testLabel.getId()))
                .andExpect(jsonPath("$[0].name").value(testLabel.getName()));
    }

    @Test
    void testGetLabelById() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);
        
        mockMvc.perform(get("/api/labels/{id}", testLabel.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabel.getId()))
                .andExpect(jsonPath("$.name").value(testLabel.getName()));
    }

    @Test
    void testCreateLabel() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        LabelCreateDTO newLabel = new LabelCreateDTO();
        newLabel.setName("New Label");

        mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLabel)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(newLabel.getName()));

        List<Label> labels = labelRepository.findAll();
        assertThat(labels).hasSize(2);
        assertThat(labels.stream().anyMatch(l -> l.getName().equals(newLabel.getName()))).isTrue();
    }

    @Test
    void testCreateLabelWithoutAuthentication() throws Exception {
        LabelCreateDTO newLabel = new LabelCreateDTO();
        newLabel.setName("New Label");

        mockMvc.perform(post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLabel)))
                .andExpect(status().isUnauthorized());

        int labelCount = labelRepository.findAll().size();
        assertThat(labelCount).isEqualTo(1);
    }

    @Test
    void testUpdateLabel() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        LabelCreateDTO updateLabel = new LabelCreateDTO();
        updateLabel.setName("Updated Label");

        mockMvc.perform(put("/api/labels/{id}", testLabel.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLabel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabel.getId()))
                .andExpect(jsonPath("$.name").value(updateLabel.getName()));

        Label updatedLabel = labelRepository.findById(testLabel.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo(updateLabel.getName());
    }

    @Test
    void testDeleteLabel() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(delete("/api/labels/{id}", testLabel.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(testLabel.getId())).isEmpty();
    }

    @Test
    void testDeleteLabelWithTasks() throws Exception {
        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Test Status");
        taskStatus.setSlug("test_status");
        taskStatus.setCreatedAt(Instant.now());
        taskStatusRepository.save(taskStatus);
        
        Task task = new Task();
        task.setName("Test Task");
        task.setTaskStatus(taskStatus);
        task.setCreatedAt(Instant.now());
        Set<Label> labels = new HashSet<>();
        labels.add(testLabel);
        task.setLabels(labels);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/labels/{id}", testLabel.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        assertThat(labelRepository.findById(testLabel.getId())).isPresent();
    }
    
    @Test
    void testUpdateAnotherUsersLabelReturns403() throws Exception {
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPassword(passwordEncoder.encode("anotherpassword"));
        anotherUser.setCreatedAt(Instant.now());
        userRepository.save(anotherUser);

        String anotherToken = getToken(anotherUser.getEmail(), "anotherpassword");
        
        LabelCreateDTO newLabel = new LabelCreateDTO();
        newLabel.setName("Another User's Label");
        
        String response = mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + anotherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLabel)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long labelId = objectMapper.readTree(response).get("id").asLong();

        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        LabelCreateDTO updateLabel = new LabelCreateDTO();
        updateLabel.setName("Hacked Label");
        
        mockMvc.perform(put("/api/labels/{id}", labelId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLabel)))
                .andExpect(status().isForbidden());

        Label unchangedLabel = labelRepository.findById(labelId).orElseThrow();
        assertThat(unchangedLabel.getName()).isEqualTo(newLabel.getName());
    }
    
    @Test
    void testDeleteAnotherUsersLabelReturns403() throws Exception {
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPassword(passwordEncoder.encode("anotherpassword"));
        anotherUser.setCreatedAt(Instant.now());
        userRepository.save(anotherUser);

        String anotherToken = getToken(anotherUser.getEmail(), "anotherpassword");
        
        LabelCreateDTO newLabel = new LabelCreateDTO();
        newLabel.setName("Another User's Label");
        
        String response = mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + anotherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLabel)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long labelId = objectMapper.readTree(response).get("id").asLong();

        String token = getToken(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(delete("/api/labels/{id}", labelId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(labelRepository.existsById(labelId)).isTrue();
    }
}