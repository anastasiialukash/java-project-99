package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;

import java.util.List;

public interface TaskStatusService {
    List<TaskStatusDTO> getAllStatuses();
    TaskStatusDTO getStatusById(Long id);
    TaskStatusDTO getStatusBySlug(String slug);
    TaskStatusDTO createStatus(TaskStatusCreateDTO taskStatusCreateDTO, String username);
    TaskStatusDTO updateTaskStatus(Long id, TaskStatusDTO taskStatusUpdateDTO);
    void deleteTaskStatus(Long id);
}
