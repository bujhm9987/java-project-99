package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    @NotBlank(message = "FirstName must not be blank")
    private String firstName;
    @NotBlank(message = "LastName must not be blank")
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotNull(message = "Password must be longer than 3 characters")
    @Size(min = 3)
    private String password;
}
