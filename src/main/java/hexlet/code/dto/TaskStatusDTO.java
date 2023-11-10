package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class TaskStatusDTO {
    private long id;
    private String name;
    private String slug;
    private Date createdAt;
}
