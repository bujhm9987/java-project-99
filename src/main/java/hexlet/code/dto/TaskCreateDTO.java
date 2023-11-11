package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskCreateDTO {

    private Integer index;

    @JsonProperty("assignee_id")
    private long assigneeId;

    @NotNull
    @Size(min = 1)
    @JsonProperty("title")
    private String name;

    @JsonProperty("content")
    private String description;

    @NotNull
    private String status;
}
