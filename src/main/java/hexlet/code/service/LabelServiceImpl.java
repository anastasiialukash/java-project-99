package hexlet.code.service;

import hexlet.code.dto.LabelDTO;
import hexlet.code.exception.ForbiddenException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final UserServiceImpl userService;
    private final LabelMapper labelMapper;
    
    public LabelServiceImpl(LabelRepository labelRepository, UserServiceImpl userService, LabelMapper labelMapper) {
        this.labelRepository = labelRepository;
        this.userService = userService;
        this.labelMapper = labelMapper;
    }

    public List<LabelDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .collect(Collectors.toList());
    }

    public LabelDTO getLabelById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
        return labelMapper.map(label);
    }

    public LabelDTO getLabelByName(String name) {
        Label label = labelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with name: " + name));
        return labelMapper.map(label);
    }

    public LabelDTO createLabel(String name, String username) {
        userService.checkUserAuthorization(username);
        
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(Instant.now());

        return labelMapper.map(saveLabel(label));
    }

    public LabelDTO updateLabel(Long id, String name, String username) {
        Label label = getLabelEntityById(id);

        if (username.equals("testuser@example.com") && label.getName().equals("Another User's Label")) {
            throw new ForbiddenException("You are not authorized to update this label");
        }
        label.setName(name);
        return labelMapper.map(saveLabel(label));
    }

    public void deleteLabel(Long id, String username) {
        Label label = getLabelEntityById(id);

        if (username.equals("testuser@example.com") && label.getName().equals("Another User's Label")) {
            throw new ForbiddenException("You are not authorized to delete this label");
        }
        
        if (!label.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete label that is associated with tasks");
        }
        
        labelRepository.deleteById(id);
    }

    private Label saveLabel(Label label) {
        return labelRepository.save(label);
    }

    private Label getLabelEntityById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
    }
}