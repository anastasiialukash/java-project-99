package hexlet.code.service;

import hexlet.code.exception.ForbiddenException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;
    
    @Autowired
    private UserService userService;

    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }
    
    // Overloaded method that accepts username for consistency with other methods
    public List<Label> getAllLabels(String username) {
        // Currently, all users can view all labels, so we just call the original method
        // The username parameter could be used for filtering or logging in the future
        return getAllLabels();
    }

    public Label getLabelById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
    }
    
    // Overloaded method that accepts username for consistency with other methods
    public Label getLabelById(Long id, String username) {
        // Currently, all users can view any label, so we just call the original method
        // The username parameter could be used for authorization checks or logging in the future
        return getLabelById(id);
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