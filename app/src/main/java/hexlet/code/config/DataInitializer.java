package hexlet.code.config;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.PasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private PasswordEncoderService passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                User adminUser = new User();
                adminUser.setEmail("hexlet@example.com");
                adminUser.setFirstName("Admin");
                adminUser.setLastName("User");
                adminUser.setPassword(passwordEncoder.encode("qwerty"));
                adminUser.setCreatedAt(Instant.now());
                adminUser.setUpdatedAt(Instant.now());
                
                userRepository.save(adminUser);
                
                System.out.println("Admin user created: hexlet@example.com / qwerty");
            }

            if (taskStatusRepository.count() == 0) {
                createDefaultTaskStatus("Draft", "draft");
                createDefaultTaskStatus("To Review", "to_review");
                createDefaultTaskStatus("To Be Fixed", "to_be_fixed");
                createDefaultTaskStatus("To Publish", "to_publish");
                createDefaultTaskStatus("Published", "published");
                
                System.out.println("Default task statuses created");
            }
        };
    }
    
    private void createDefaultTaskStatus(String name, String slug) {
        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        status.setCreatedAt(Instant.now());
        taskStatusRepository.save(status);
    }
}