package hexlet.code.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private LocalDate createdAt;
}
