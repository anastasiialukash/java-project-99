package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskCreateDTO {
    @Size(min = 1)
    String name;
    @NotNull
    @Size(min = 1)
    String slug;
    private LocalDate createdAt;
}
