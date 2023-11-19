package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("${base-url}" + "/task_statuses")
public class TaskStatusController {
    @Autowired
    private TaskStatusService taskStatusService;

    @Operation(
            summary = "Get list of task statuses",
            description = "Get list of task statuses available in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of all task statuses",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = TaskStatusDTO.class))
                                    )
                            })
            }
    )
    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> getTaskStatuses() {
        var taskStatuses = taskStatusService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(taskStatuses.size()))
                .body(taskStatuses);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Create task status",
            description = "Creating a new task status",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Task status successfully created",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskStatusDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid data for creating a new task status",
                            content = @Content)
            }
    )
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO createTaskStatus(@Valid @RequestBody TaskStatusCreateDTO taskStatusData) {
        return taskStatusService.create(taskStatusData);
    }

    @Operation(
            summary = "Get task status by ID",
            description = "Get task status available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task status found by ID",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskStatusDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "404", description = "Task status with that ID not found",
                            content = @Content)
            }
    )
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskStatusDTO> showTaskStatus(@PathVariable long id) {
        return ResponseEntity.ok()
                .body(taskStatusService.findById(id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Update task status by ID",
            description = "Update task status available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task status updated successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = TaskStatusDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid task status update data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task status with that ID not found",
                            content = @Content)
            }
    )
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskStatusDTO> updateTaskStatus(@PathVariable long id,
                                                          @RequestBody @Valid TaskStatusUpdateDTO taskStatusData) {
        return ResponseEntity.ok()
                .body(taskStatusService.update(taskStatusData, id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Delete task status by ID",
            description = "Deleting task status from the system by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Task status deleted successfully",
                            content = @Content),
                    @ApiResponse(responseCode = "400",
                            description = "There are active tasks with this task status",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task status with that ID not found",
                            content = @Content)
            }
    )
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskStatus(@PathVariable long id) {
        taskStatusService.delete(id);
    }
}
