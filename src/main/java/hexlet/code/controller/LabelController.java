package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.service.LabelServiceImpl;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelServiceImpl labelService;

    public LabelController(LabelServiceImpl labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public ResponseEntity<List<LabelDTO>> getAllLabels() {
        List<LabelDTO> labels = labelService.getAllLabels();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @GetMapping("/{id}")
    public LabelDTO getLabelById(@PathVariable Long id) {
        return labelService.getLabelById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelCreateDTO labelCreateDTO, Authentication authentication) {
        String username = authentication.getName();
        return labelService.createLabel(labelCreateDTO.getName(), username);
    }

    @PutMapping("/{id}")
    public LabelDTO updateLabel(@PathVariable Long id, @Valid @RequestBody LabelCreateDTO labelCreateDTO, Authentication authentication) {
        String username = authentication.getName();
        return labelService.updateLabel(id, labelCreateDTO.getName(), username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        labelService.deleteLabel(id, username);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException exception) {
        return exception.getMessage();
    }
}