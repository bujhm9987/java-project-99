package hexlet.code.controller;


import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.service.LabelService;
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
@RequestMapping(LabelController.URL)
@RequiredArgsConstructor
public final class LabelController {

    public static final String URL = "/api/labels";

    private final LabelService labelService;

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get list of task labels",
            description = "Get list of task labels available in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of all labels",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = LabelDTO.class))
                                    )
                            })
            }
    )
    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> getLabels() {
        var labels = labelService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Create task label",
            description = "Creating a new task label",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Task labels successfully created",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LabelDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid data for creating a new task label",
                            content = @Content)
            }
    )
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelCreateDTO labelData) {
        return labelService.create(labelData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get task label by ID",
            description = "Get task label available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task label found by ID",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LabelDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "404", description = "Task label with that ID not found",
                            content = @Content)
            }
    )
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LabelDTO> showLabel(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(labelService.findById(id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Update task label by ID",
            description = "Update task label available in the system by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task label updated successfully",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LabelDTO.class)
                                    )
                            }),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid task label update data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task label with that ID not found",
                            content = @Content)
            }
    )
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LabelDTO> updateLabel(@PathVariable Long id,
                                              @RequestBody @Valid LabelUpdateDTO labelData) {
        return ResponseEntity.ok()
                .body(labelService.update(labelData, id));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Delete task label by ID",
            description = "Deleting task label from the system by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Task label deleted successfully",
                            content = @Content),
                    @ApiResponse(responseCode = "400",
                            description = "There are active tasks with this task label",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task label with that ID not found",
                            content = @Content)
            }
    )
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id) {
        labelService.delete(id);
    }
}
