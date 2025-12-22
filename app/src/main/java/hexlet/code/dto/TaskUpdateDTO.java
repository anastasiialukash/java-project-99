package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private Integer index;
    
    @Size(min = 1)
    private String title;
    
    private String content;
    
    private String status;
    
    private Long assignee_id;
    
    private Set<Long> labelIds;
}