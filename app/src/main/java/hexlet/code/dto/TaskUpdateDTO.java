package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskUpdateDTO {
    private Integer index;
    
    @Size(min = 1)
    private String title;
    
    private String content;
    
    private String status;
    
    private Long assignee_id;
}