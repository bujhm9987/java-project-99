package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    @Autowired
    private TaskSpecification specBuilder;

    public List<TaskDTO> getAll(TaskParamsDTO paramsDTO) {
        var spec = specBuilder.build(paramsDTO);
        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(taskMapper::map)
                .collect(Collectors.toList());
    }

    public TaskDTO create(TaskCreateDTO taskData) {

        var taskLabelIDs = taskData.getTaskLabelIds();
        if (taskLabelIDs != null) {
            var labelIds = taskLabelIDs.stream()
                    .map(i -> labelRepository.findById(i)
                            .orElseThrow()
                            .getId())
                    .collect(Collectors.toSet());
            taskData.setTaskLabelIds(labelIds);
        } else {
            taskData.setTaskLabelIds(new HashSet<>());
        }

        var task = taskMapper.map(taskData);

        var taskStatusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(taskStatusSlug)
                .orElseThrow();
        task.setTaskStatus(taskStatus);

        var taskDataUserId = taskData.getAssigneeId();
        if (taskDataUserId != 0) {
            var assignee = userRepository.findById(taskDataUserId)
                    .orElseThrow();
            task.setAssignee(assignee);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow();
        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow();

        taskMapper.update(taskData, task);

        var taskDataSlug = taskData.getStatus();
        if (taskDataSlug != null) {
            var status = taskStatusRepository.findBySlug((taskDataSlug).get())
                    .orElseThrow();
            task.setTaskStatus(status);
        }

        var taskDataUserId = taskData.getAssigneeId();
        if (taskDataUserId != null) {
            var assignee = userRepository.findById((taskDataUserId).get())
                    .orElseThrow();
            task.setAssignee(assignee);
        }

        var taskLabelIds = taskData.getTaskLabelIds();
        if (taskLabelIds != null) {
            var newLabels = taskLabelIds.get().stream()
                    .map(i -> labelRepository.findById(i)
                            .orElseThrow())
                    .collect(Collectors.toSet());
            task.setLabels(newLabels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.findById(id)
                .orElseThrow();
        taskRepository.deleteById(id);
    }
}
