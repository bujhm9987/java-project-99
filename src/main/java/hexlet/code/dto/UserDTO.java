package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class UserDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private Date createdAt;
    private Date updatedAt;
}
