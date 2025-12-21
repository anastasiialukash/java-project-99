package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserService userService;

    public List<TaskStatusDTO> getAllStatuses() {
        return taskStatusRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TaskStatusDTO getStatusById(Long id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with id: " + id));
        return convertToDTO(status);
    }
    
    public TaskStatusDTO getStatusBySlug(String slug) {
        TaskStatus status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with slug: " + slug));
        return convertToDTO(status);
    }

    public TaskStatusDTO createStatus(TaskCreateDTO taskStatusCreateDTO) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(taskStatusCreateDTO.getName());
        taskStatus.setSlug(taskStatusCreateDTO.getSlug());
        taskStatus.setCreatedAt(Instant.now());

        TaskStatus savedTaskStatus = taskStatusRepository.save(taskStatus);
        return convertToDTO(savedTaskStatus);
    }

    public TaskStatusDTO updateTaskStatus(Long id, TaskStatusDTO taskStatusUpdateDTO, String username) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with id: " + id));

        userService.checkUserAuthorization(username);

        if (taskStatusUpdateDTO.getName() != null) {
            taskStatus.setName(taskStatusUpdateDTO.getName());
        }
        if (taskStatusUpdateDTO.getSlug() != null) {
            taskStatus.setSlug(taskStatusUpdateDTO.getSlug());
        }

        TaskStatus updatedTaskStatus = taskStatusRepository.save(taskStatus);
        return convertToDTO(updatedTaskStatus);
    }

    public void deleteTaskStatus(Long id, String username) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with id: " + id));

        userService.checkUserAuthorization(username);

        taskStatusRepository.deleteById(id);
    }

    private TaskStatusDTO convertToDTO(TaskStatus taskStatus) {
        TaskStatusDTO dto = new TaskStatusDTO();
        dto.setId(taskStatus.getId());
        dto.setName(taskStatus.getName());
        dto.setSlug(taskStatus.getSlug());

        if (taskStatus.getCreatedAt() != null) {
            dto.setCreatedAt(LocalDate.ofInstant(taskStatus.getCreatedAt(), ZoneId.systemDefault()));
        }
        return dto;
    }
}
