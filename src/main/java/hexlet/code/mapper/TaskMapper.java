package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {ReferenceMapper.class}
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusToTaskStatus")
    @Mapping(target = "assignee", source = "assignee_id", qualifiedByName = "assigneeIdToUser")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "labelIdsToLabels")
    @Mapping(target = "createdAt", expression = "java(getCurrentTime())")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "assignee_id", source = "assignee.id")
    @Mapping(target = "taskLabelIds", source = "labels", qualifiedByName = "labelsToLabelIds")
    @Mapping(target = "createdAt", expression = "java(convertToLocalDate(model.getCreatedAt()))")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusToTaskStatus")
    @Mapping(target = "assignee", source = "assignee_id", qualifiedByName = "assigneeIdToUser")
    @Mapping(target = "labels", source = "labelIds", qualifiedByName = "labelIdsSetToLabels")
    @Mapping(target = "createdAt", ignore = true)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Named("statusToTaskStatus")
    protected TaskStatus statusToTaskStatus(String status) {
        if (status == null) {
            return null;
        }
        return taskStatusRepository.findBySlug(status)
                .orElse(null);
    }

    @Named("assigneeIdToUser")
    protected User assigneeIdToUser(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElse(null);
    }

    @Named("labelIdsToLabels")
    protected Set<Label> labelIdsToLabels(List<Long> labelIds) {
        if (labelIds == null) {
            return null;
        }
        return labelIds.stream()
                .map(id -> labelRepository.findById(id).orElse(null))
                .filter(label -> label != null)
                .collect(Collectors.toSet());
    }

    @Named("labelsToLabelIds")
    protected List<Long> labelsToLabelIds(Set<Label> labels) {
        if (labels == null) {
            return null;
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }
    
    @Named("labelIdsSetToLabels")
    protected Set<Label> labelIdsSetToLabels(Set<Long> labelIds) {
        if (labelIds == null) {
            return null;
        }
        return labelIds.stream()
                .map(id -> labelRepository.findById(id).orElse(null))
                .filter(label -> label != null)
                .collect(Collectors.toSet());
    }

    protected Instant getCurrentTime() {
        return Instant.now();
    }

    protected LocalDate convertToLocalDate(Instant instant) {
        return instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }
}