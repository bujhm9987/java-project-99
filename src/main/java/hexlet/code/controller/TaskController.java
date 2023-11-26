package hexlet.code.controller;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping(TaskController.URL)
@RequiredArgsConstructor
public final class TaskController {

    public static final String URL = "/api/tasks";

    private final TaskService taskService;

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get list of tasks",
            description = "Get list of tasks available in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of all tasks",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class))
                                    )
                            })
            }
    )
    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskDTO>> getTasks(@Parameter(description = "Filtering parameters for task list")
                                                      TaskParamsDTO paramsDTO) {
        var tasks = taskService.getAll(paramsDTO);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Create task",
            description = "Creating a new task",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Task successfully created",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid data for creating a new task",
                            content = @Content)
            }
    )
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody TaskCreateDTO taskData) {
        return taskService.create(taskData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get task by ID",
            description = "Get task available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task found by ID",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "404", description = "Task with that ID not found",
                            content = @Content)
            }
    )
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskDTO> showTask(@PathVariable long id) {
        return ResponseEntity.ok()
                .body(taskService.findById(id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Update task by ID",
            description = "Update task available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task updated successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid task update data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task with that ID not found",
                            content = @Content)
            }
    )
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskDTO> updateTask(@PathVariable long id,
                                                    @RequestBody @Valid TaskUpdateDTO taskData) {
        return ResponseEntity.ok()
                .body(taskService.update(taskData, id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Delete task by ID",
            description = "Deleting task from the system by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Task deleted successfully",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task with that ID not found",
                            content = @Content)
            }
    )
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable long id) {
        taskService.delete(id);
    }
}
