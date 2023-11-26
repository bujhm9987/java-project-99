package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {

    private Integer index;

    @JsonProperty("assignee_id")
    private long assigneeId;

    @NotBlank
    @JsonProperty("title")
    private String name;

    @JsonProperty("content")
    private String description;

    @NotNull
    private String status;

    private Set<Long> taskLabelIds;
}
