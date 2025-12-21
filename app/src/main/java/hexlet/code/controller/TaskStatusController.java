package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.service.TaskStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/task_statuses")
public class TaskStatusController {
    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping
    public ResponseEntity<List<TaskStatusDTO>> getAllStatuses() {
        List<TaskStatusDTO> statuses = taskStatusService.getAllStatuses();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(statuses.size())).body(statuses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> getStatusById(@PathVariable Long id) {
        TaskStatusDTO status = taskStatusService.getStatusById(id);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TaskStatusDTO> getStatusBySlug(@PathVariable String slug) {
        TaskStatusDTO status = taskStatusService.getStatusBySlug(slug);
        return ResponseEntity.ok(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO createStatus(@Valid @RequestBody TaskStatusCreateDTO taskStatusCreateDTO) {
        return taskStatusService.createStatus(taskStatusCreateDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> updateStatus(@PathVariable Long id, 
                                                     @Valid @RequestBody TaskStatusDTO taskStatusUpdateDTO, 
                                                     Authentication authentication) {
        String username = authentication.getName();
        TaskStatusDTO updatedTaskStatus = taskStatusService.updateTaskStatus(id, taskStatusUpdateDTO, username);
        return ResponseEntity.ok(updatedTaskStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskStatus(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        taskStatusService.deleteTaskStatus(id, username);
    }
}