package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    private String title;
    private String content;
    private String status;
    private Long assignee_id;
    private LocalDate createdAt;
}