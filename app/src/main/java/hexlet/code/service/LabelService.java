package hexlet.code.service;

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

    public Label createLabel(String name) {
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(Instant.now());
        return labelRepository.save(label);
    }

    public Label updateLabel(Long id, String name) {
        Label label = getLabelById(id);
        label.setName(name);
        return labelRepository.save(label);
    }

    public void deleteLabel(Long id) {
        Label label = getLabelById(id);
        
        if (!label.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete label that is associated with tasks");
        }
        
        labelRepository.deleteById(id);
    }
}