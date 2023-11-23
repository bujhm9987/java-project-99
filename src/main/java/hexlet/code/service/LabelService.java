package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ConstraintViolationException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;

     @Autowired
     private TaskRepository taskRepository;

    @Autowired
    private LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        var labels = labelRepository.findAll();
        return labels.stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO create(LabelCreateDTO labelData) {
        var labelName = labelData.getName();
        var findName = labelRepository.findByName(labelName);
        if (findName.isPresent()) {
            throw new ConstraintViolationException(String.format("Label with name %s already exists", labelName));
        }
        var label = labelMapper.map(labelData);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));
        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO labelData, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));
        var labelName = labelData.getName();
        var findName = labelRepository.findByName(labelName.get());
        if (findName.isPresent()) {
            throw new ConstraintViolationException(String.format("Label with name %s already exists", labelName.get()));
        }
        labelMapper.update(labelData, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));
        var tasks = taskRepository.findAll();

        var findTask = tasks.stream()
                .flatMap(task -> task.getLabels().stream())
                .map(Label::getId)
                .filter(id::equals)
                .findAny();
        if (findTask.isEmpty()) {
            labelRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("Label with id %s has active tasks", id));
        }

    }
}
