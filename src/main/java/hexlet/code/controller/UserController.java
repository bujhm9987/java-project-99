package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${base-url}" + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get list of users",
            description = "Get list of users registered in the system",
            responses = {
                    @ApiResponse(
                    responseCode = "200",
                    description = "List of all users",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))
                            )
                    })
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> getUsers() {
        var users = userService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(users);
    }

    @Operation(
            summary = "New User Registration",
            description = "Registering new user in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully created",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UserDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid data for user registration in the system",
                            content = @Content)
            }
    )
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody UserCreateDTO userData) {
        return userService.create(userData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get user by ID",
            description = "Get user registered in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found by ID",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UserDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "404", description = "User with that ID not found",
                            content = @Content)
            }
    )
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDTO> showUser(@PathVariable long id) {
        return ResponseEntity.ok()
                .body(userService.findById(id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Update user by ID",
            description = "Update user in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User data has been successfully updated",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UserDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid user update data",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Insufficient rights to update this user's data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "User with that ID not found",
                            content = @Content)
            }
    )
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDTO> updateUser(@PathVariable long id, @RequestBody @Valid UserUpdateDTO userData) {
        return ResponseEntity.ok()
                .body(userService.update(userData, id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Delete user by ID",
            description = "Deleting registered user from the system by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully",
                            content = @Content),
                    @ApiResponse(responseCode = "403",
                            description = "There are insufficient privileges to delete this user",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "User with that ID not found",
                            content = @Content)
            }
    )
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        userService.delete(id);
    }
}
