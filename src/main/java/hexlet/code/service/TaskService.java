package hexlet.code.service;

import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ConstraintViolationException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.mapper.TaskMapperImpl;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public List<TaskDTO> getAll() {
        var tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::map)
                .collect(Collectors.toList());
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        var task = taskMapper.map(taskData);

        var taskStatusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(taskStatusSlug)
                .orElseThrow(() -> new ConstraintViolationException(String
                        .format("Status with slug %s not found", taskStatusSlug)));
        task.setTaskStatus(taskStatus);

        var taskDataUserId = taskData.getAssigneeId();
        if (taskDataUserId != 0) {
            var assignee = userRepository.findById(taskDataUserId)
                    .orElseThrow(() -> new ConstraintViolationException(String
                            .format("User with id %s not found", taskDataUserId)));
            task.setAssignee(assignee);
        }

        var taskLabels = taskData.getTaskLabelIds();
        if (!taskLabels.isEmpty()) {
            var labels = taskLabels.stream()
                            .map(i -> labelRepository.findById(i)
                                            .orElseThrow(() -> new ConstraintViolationException(String
                                            .format("Label with id %s not found", i))))
                            .collect(Collectors.toList());
            task.setTaskLabels(labels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));

        taskMapper.update(taskData, task);

        var taskDataSlug = taskData.getStatus();
        if (taskDataSlug != null) {
            var status = taskStatusRepository.findBySlug((taskDataSlug).get())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            task.setTaskStatus(status);
        }

        var taskDataUserId = taskData.getAssigneeId();
        if (taskDataUserId != null) {
            var assignee = userRepository.findById((taskDataUserId).get())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            task.setAssignee(assignee);
        }

        var taskLabelIds = taskData.getTaskLabelIds();
        if (taskLabelIds != null) {
            var newLabels = taskLabelIds.get().stream()
                    .map(i -> labelRepository.findById(i)
                            .orElseThrow(() -> new ConstraintViolationException(String
                                    .format("Label with id %s not found", i))))
                    .collect(Collectors.toList());
            task.setTaskLabels(newLabels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        taskRepository.deleteById(id);
    }
}
