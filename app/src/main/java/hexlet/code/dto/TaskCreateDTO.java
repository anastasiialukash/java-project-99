package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;
    
    @NotBlank
    @Size(min = 1)
    private String title;
    
    private String content;
    
    @NotNull
    private String status;
    
    private Long assignee_id;
    
    private Set<Long> labelIds;
}