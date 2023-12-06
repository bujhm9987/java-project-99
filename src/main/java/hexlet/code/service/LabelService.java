package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;

import hexlet.code.mapper.LabelMapper;
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
        /*var labelName = labelData.getName();
        var findName = labelRepository.findByName(labelName);
        if (findName.isPresent()) {
            throw new ConstraintViolationException(String.format("Label with name %s already exists", labelName));
        }*/
        var label = labelMapper.map(labelData);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow();
        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO labelData, Long id) {
        /*var label = labelRepository.findById(id)
                .orElseThrow();*/
        /*var labelName = labelData.getName();
        var findName = labelRepository.findByName(labelName.get());
        if (findName.isPresent()) {
            throw new ConstraintViolationException(String.format("Label with name %s already exists", labelName.get()));
        }*/
        var label = labelRepository.findById(id)
                .orElseThrow();


        labelMapper.update(labelData, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.findById(id)
                .orElseThrow();
        labelRepository.deleteById(id);
        /*var tasks = taskRepository.findAll();

        var findTask = tasks.stream()
                .flatMap(task -> task.getLabels().stream())
                .map(Label::getId)
                .filter(id::equals)
                .findAny();
        if (findTask.isEmpty()) {
            labelRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("Label with id %s has active tasks", id));
        }*/
    }
}
