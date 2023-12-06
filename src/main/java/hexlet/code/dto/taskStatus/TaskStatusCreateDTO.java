package hexlet.code.dto.taskStatus;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskStatusCreateDTO {

    @Size(min = 1)
    @Column(unique = true)
    private String name;

    @Size(min = 1)
    @Column(unique = true)
    private String slug;
}
