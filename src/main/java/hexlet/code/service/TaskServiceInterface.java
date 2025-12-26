package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;

import java.util.List;

public interface TaskServiceInterface {
    List<TaskDTO> getAllTasks();
    List<TaskDTO> getFilteredTasks(String titleCont, Long assigneeId, String status, Long labelId);
    TaskDTO getTaskById(Long id);
    TaskDTO createTask(TaskCreateDTO taskCreateDTO, String username);
    TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO, String username);
    void deleteTask(Long id, String username);
}