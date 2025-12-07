package hexlet.code.config;

import hexlet.code.model.User;
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
        };
    }
}