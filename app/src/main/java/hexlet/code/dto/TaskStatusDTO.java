package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskStatusDTO {
    Long id;
    @Size(min = 1)
    String name;
    @Size(min = 1)
    String slug;
    private LocalDate createdAt;
}
