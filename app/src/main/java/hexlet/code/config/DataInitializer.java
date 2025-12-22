package hexlet.code.config;

import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) {
        createDefaultLabelsIfNotExist();
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
}