package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    private String title;
    private String content;
    private String status;
    private Long assignee_id;
    private Set<LabelDTO> labels;
    private LocalDate createdAt;
}