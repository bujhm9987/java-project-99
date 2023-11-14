package hexlet.code.controller;


import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.service.LabelService;
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
@RequestMapping("${base-url}" + "/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> getLabels() {
        var labels = labelService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelCreateDTO labelData) {
        return labelService.create(labelData);
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LabelDTO> showLabel(@PathVariable long id) {
        return ResponseEntity.ok()
                .body(labelService.findById(id));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LabelDTO> updateLabel(@PathVariable long id,
                                              @RequestBody @Valid LabelUpdateDTO labelData) {
        return ResponseEntity.ok()
                .body(labelService.update(labelData, id));
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable long id) {
        labelService.delete(id);
    }
}
