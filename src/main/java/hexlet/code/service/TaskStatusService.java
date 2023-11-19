package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ConstraintViolationException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAll() {
        var taskStatuses = taskStatusRepository.findAll();
        return taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskStatusData) {
        var statusName = taskStatusData.getName();
        var findName = taskStatusRepository.findByName(statusName);
        if (findName.isPresent()) {
            throw new ConstraintViolationException(String.format("TaskStatus with name %s already exists", findName));
        }
        var statusSlug = taskStatusData.getSlug();
        var findSlug = taskStatusRepository.findByName(statusSlug);
        if (findSlug.isPresent()) {
            throw new ConstraintViolationException(String.format("TaskStatus with name %s already exists", findSlug));
        }
        var taskStatus = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO findById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        var statusSlug = taskStatusData.getSlug();
        if (statusSlug != null) {
            var findSlug = taskStatusRepository.findByName(statusSlug.get());
            if (findSlug.isPresent()) {
                throw new ConstraintViolationException(String.format("TaskStatus with name %s already exists", findSlug));
            }
        }
        taskStatusMapper.update(taskStatusData, taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void delete(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        var tasks = taskStatus.getTasks();
        if (tasks.isEmpty()) {
            taskStatusRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("TaskStatus with id %s is used in tasks", id));
        }
    }
}
