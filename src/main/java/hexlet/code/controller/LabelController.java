package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.model.Label;
import hexlet.code.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public ResponseEntity<List<LabelDTO>> getAllLabels(Authentication authentication) {
        String username = authentication.getName();
        List<LabelDTO> labels = labelService.getAllLabels(username).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @GetMapping("/{id}")
    public LabelDTO getLabelById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Label label = labelService.getLabelById(id, username);
        return convertToDTO(label);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelCreateDTO labelCreateDTO, Authentication authentication) {
        String username = authentication.getName();
        Label label = labelService.createLabel(labelCreateDTO.getName(), username);
        return convertToDTO(label);
    }

    @PutMapping("/{id}")
    public LabelDTO updateLabel(@PathVariable Long id, @Valid @RequestBody LabelCreateDTO labelCreateDTO, Authentication authentication) {
        String username = authentication.getName();
        Label label = labelService.updateLabel(id, labelCreateDTO.getName(), username);
        return convertToDTO(label);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        labelService.deleteLabel(id, username);
    }

    private LabelDTO convertToDTO(Label label) {
        LabelDTO dto = new LabelDTO();
        dto.setId(label.getId());
        dto.setName(label.getName());
        
        if (label.getCreatedAt() != null) {
            dto.setCreatedAt(label.getCreatedAt());
        }
        
        return dto;
    }
    
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException exception) {
        return exception.getMessage();
    }
}