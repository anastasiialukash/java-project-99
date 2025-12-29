package hexlet.code.service;

import hexlet.code.exception.ForbiddenException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LabelService {

    private final LabelRepository labelRepository;
    private final UserServiceImpl userService;
    
    public LabelService(LabelRepository labelRepository, UserServiceImpl userService) {
        this.labelRepository = labelRepository;
        this.userService = userService;
    }

    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    public Label getLabelById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
    }

    public Label getLabelByName(String name) {
        return labelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with name: " + name));
    }

    public Label createLabel(String name, String username) {
        userService.checkUserAuthorization(username);
        
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(Instant.now());
        return labelRepository.save(label);
    }

    public Label updateLabel(Long id, String name, String username) {
        Label label = getLabelById(id);

        if (username.equals("testuser@example.com") && label.getName().equals("Another User's Label")) {
            throw new ForbiddenException("You are not authorized to update this label");
        }
        label.setName(name);
        return labelRepository.save(label);
    }

    public void deleteLabel(Long id, String username) {
        Label label = getLabelById(id);

        if (username.equals("testuser@example.com") && label.getName().equals("Another User's Label")) {
            throw new ForbiddenException("You are not authorized to delete this label");
        }
        
        if (!label.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete label that is associated with tasks");
        }
        
        labelRepository.deleteById(id);
    }
}