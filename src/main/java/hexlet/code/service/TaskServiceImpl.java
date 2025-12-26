package hexlet.code.service;

import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ForbiddenException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskServiceInterface {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;

    @Override
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getFilteredTasks(String titleCont, Long assigneeId, String status, Long labelId) {
        List<Task> filteredTasks = taskRepository.findByFilters(titleCont, assigneeId, status, labelId);
        return filteredTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    @Override
    public TaskDTO createTask(TaskCreateDTO taskCreateDTO, String username) {
        Task task = new Task();
        task.setName(taskCreateDTO.getTitle());
        task.setDescription(taskCreateDTO.getContent());
        task.setIndex(taskCreateDTO.getIndex());

        TaskStatus taskStatus = taskStatusRepository.findBySlug(taskCreateDTO.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with slug: " + taskCreateDTO.getStatus()));
        task.setTaskStatus(taskStatus);

        if (taskCreateDTO.getAssignee_id() != null) {
            User assignee = userRepository.findById(taskCreateDTO.getAssignee_id())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskCreateDTO.getAssignee_id()));
            task.setAssignee(assignee);
        }

        if (taskCreateDTO.getTaskLabelIds() != null && !taskCreateDTO.getTaskLabelIds().isEmpty()) {
            Set<Label> labels = taskCreateDTO.getTaskLabelIds().stream()
                    .map(labelId -> labelRepository.findById(labelId)
                            .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId)))
                    .collect(Collectors.toSet());
            task.setLabels(labels);
        }
        
        task.setCreatedAt(Instant.now());
        
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getAssignee() != null && !task.getAssignee().getEmail().equals(username)) {
            throw new ForbiddenException("You are not authorized to update this task");
        }
        
        if (taskUpdateDTO.getTitle() != null) {
            task.setName(taskUpdateDTO.getTitle());
        }
        
        if (taskUpdateDTO.getContent() != null) {
            task.setDescription(taskUpdateDTO.getContent());
        }
        
        if (taskUpdateDTO.getIndex() != null) {
            task.setIndex(taskUpdateDTO.getIndex());
        }
        
        if (taskUpdateDTO.getStatus() != null) {
            TaskStatus taskStatus = taskStatusRepository.findBySlug(taskUpdateDTO.getStatus())
                    .orElseThrow(() -> new ResourceNotFoundException("Task status not found with slug: " + taskUpdateDTO.getStatus()));
            task.setTaskStatus(taskStatus);
        }
        
        if (taskUpdateDTO.getAssignee_id() != null) {
            User assignee = userRepository.findById(taskUpdateDTO.getAssignee_id())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskUpdateDTO.getAssignee_id()));
            task.setAssignee(assignee);
        }

        if (taskUpdateDTO.getLabelIds() != null) {
            Set<Label> labels = taskUpdateDTO.getLabelIds().stream()
                    .map(labelId -> labelRepository.findById(labelId)
                            .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId)))
                    .collect(Collectors.toSet());
            task.setLabels(labels);
        }
        
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getAssignee() != null && !task.getAssignee().getEmail().equals(username)) {
            throw new ForbiddenException("You are not authorized to delete this task");
        }
        
        taskRepository.deleteById(id);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setIndex(task.getIndex());
        dto.setTitle(task.getName());
        dto.setContent(task.getDescription());
        dto.setStatus(task.getTaskStatus().getSlug());
        
        if (task.getAssignee() != null) {
            dto.setAssignee_id(task.getAssignee().getId());
        }
        
        if (task.getCreatedAt() != null) {
            dto.setCreatedAt(LocalDate.ofInstant(task.getCreatedAt(), ZoneId.systemDefault()));
        }

        if (task.getLabels() != null && !task.getLabels().isEmpty()) {
            dto.setTaskLabelIds(
                    task.getLabels()
                            .stream()
                            .map(Label::getId)
                            .toList()
            );
        }
        
        return dto;
    }
    
    private LabelDTO convertToLabelDTO(Label label) {
        LabelDTO dto = new LabelDTO();
        dto.setId(label.getId());
        dto.setName(label.getName());
        
        if (label.getCreatedAt() != null) {
            dto.setCreatedAt(label.getCreatedAt());
        }
        
        return dto;
    }
}