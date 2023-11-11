package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public List<TaskDTO> getAll() {
        var tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        var task = taskMapper.map(taskData);
        var taskStatusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(taskStatusSlug).get();
        task.setTaskStatus(taskStatus);

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id \" + id + \" not found"));
        return taskMapper.map(task);
    }

 /*   public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id \" + id + \" not found"));

        taskMapper.update(taskData, task);

        if (taskData.getStatus().isPresent()) {
            var taskStatusSlug = taskData.getStatus();
            var taskStatus = taskStatusRepository.findBySlug(String.valueOf(taskStatusSlug)).get();
            task.setTaskStatus(taskStatus);
        }

        task.setTaskStatus(taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }*/

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
