package hexlet.code.config;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) {
        createDefaultLabelsIfNotExist();
        createDefaultTaskStatusesIfNotExist();
        createDefaultUserIfNotExist();
        createSampleTasks();
    }

    private void createDefaultLabelsIfNotExist() {
        List<String> defaultLabelNames = Arrays.asList("feature", "bug");
        
        for (String labelName : defaultLabelNames) {
            if (labelRepository.findByName(labelName).isEmpty()) {
                Label label = new Label();
                label.setName(labelName);
                label.setCreatedAt(Instant.now());
                labelRepository.save(label);
            }
        }
    }
    
    private void createDefaultTaskStatusesIfNotExist() {
        List<String[]> defaultStatuses = Arrays.asList(
            new String[]{"New", "new"},
            new String[]{"In Progress", "in_progress"},
            new String[]{"Done", "done"}
        );
        
        for (String[] statusInfo : defaultStatuses) {
            String name = statusInfo[0];
            String slug = statusInfo[1];
            
            if (taskStatusRepository.findBySlug(slug).isEmpty()) {
                var taskStatus = new hexlet.code.model.TaskStatus();
                taskStatus.setName(name);
                taskStatus.setSlug(slug);
                taskStatus.setCreatedAt(Instant.now());
                taskStatusRepository.save(taskStatus);
            }
        }
    }

    private void createDefaultUserIfNotExist() {
        String email = "hexlet@example.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            UserCreateDTO userData = new UserCreateDTO();
            userData.setEmail(email);
            userData.setFirstName("Hexlet");
            userData.setLastName("User");
            userData.setPassword("qwerty");
            userService.createUser(userData);
        }
    }

    private void createSampleTasks() {
        if (taskRepository.count() > 0) {
            return;
        }

        User user = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        var taskStatus = taskStatusRepository.findBySlug("new").orElseThrow();
        var faker = new Faker();

        IntStream.range(1, 10).forEach(i -> {
            TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
            taskCreateDTO.setTitle(faker.book().title());
            taskCreateDTO.setContent(String.join("\n", faker.lorem().paragraphs(5)));
            taskCreateDTO.setStatus(taskStatus.getSlug());
            taskCreateDTO.setAssignee_id(user.getId());
            taskService.createTask(taskCreateDTO);
        });
    }
}