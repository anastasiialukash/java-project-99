package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskStatusServiceImpl implements TaskStatusService {
    private final TaskStatusRepository taskStatusRepository;
    private final UserServiceImpl userService;
    private final TaskStatusMapper taskStatusMapper;
    
    public TaskStatusServiceImpl(
            TaskStatusRepository taskStatusRepository, 
            UserServiceImpl userService,
            TaskStatusMapper taskStatusMapper) {
        this.taskStatusRepository = taskStatusRepository;
        this.userService = userService;
        this.taskStatusMapper = taskStatusMapper;
    }

    public List<TaskStatusDTO> getAllStatuses() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .collect(Collectors.toList());
    }

    public TaskStatusDTO getStatusById(Long id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with id: " + id));
        return taskStatusMapper.map(status);
    }
    
    public TaskStatusDTO getStatusBySlug(String slug) {
        TaskStatus status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with slug: " + slug));
        return taskStatusMapper.map(status);
    }

    public TaskStatusDTO createStatus(TaskStatusCreateDTO taskStatusCreateDTO, String username) {
        userService.checkUserAuthorization(username);
        
        TaskStatus taskStatus = taskStatusMapper.map(taskStatusCreateDTO);
        TaskStatus savedTaskStatus = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(savedTaskStatus);
    }

    public TaskStatusDTO updateTaskStatus(Long id, TaskStatusDTO taskStatusUpdateDTO) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found with id: " + id));

        taskStatusMapper.update(taskStatusUpdateDTO, taskStatus);
        TaskStatus updatedTaskStatus = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(updatedTaskStatus);
    }

    public void deleteTaskStatus(Long id) {
        taskStatusRepository.deleteById(id);
    }
}