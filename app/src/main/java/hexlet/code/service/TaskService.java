package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    public TaskDTO createTask(TaskCreateDTO taskCreateDTO) {
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
        
        task.setCreatedAt(Instant.now());
        
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
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
        
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
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
        
        return dto;
    }
}