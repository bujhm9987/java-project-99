package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.stream.Collectors;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private Task testTask;

    private User testUser;

    private TaskStatus testTaskStatus;

    @Value("${base-url}" + "/tasks")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setLabels(Set.of(generatedTestLabel(), generatedTestLabel()));
        taskRepository.save(testTask);
    }

    @AfterEach
    public void clear() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    private Label generatedTestLabel() {
        var testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
        return labelRepository.findByName(
                testLabel.getName()).orElse(null);
    }

    @Test
    public void testGetAllTasks() throws Exception {

        var result = mockMvc.perform(get(url).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testGetTask() throws Exception {

        var request = get(url + "/{id}", testTask.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("taskLabelIds").isEqualTo(testTask.getLabels().stream()
                        .map(Label::getId).collect(Collectors.toSet()))
        );
    }

    @Test
    public void testCreateTask() throws Exception {

        var newTestTask = Instancio.of(modelGenerator.getTaskModel()).create();
        newTestTask.setTaskStatus(testTaskStatus);
        newTestTask.setAssignee(testUser);
        newTestTask.setLabels(Set.of(generatedTestLabel(), generatedTestLabel()));

        var dto = mapper.mapToCreateDTO(newTestTask);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(
                newTestTask.getName()).orElseThrow();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(newTestTask.getName());
        assertThat(task.getIndex()).isEqualTo(newTestTask.getIndex());
        assertThat(task.getDescription()).isEqualTo(newTestTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(newTestTask.getTaskStatus().getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(newTestTask.getAssignee().getId());
        assertThat(task.getLabels().stream().map(Label::getId).collect(Collectors.toSet()))
                .isEqualTo(newTestTask.getLabels().stream().map(Label::getId).collect(Collectors.toSet()));
    }

    @Test
    public void testCreateTaskWithNotValidName() throws Exception {
        testTask.setName("");
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTaskWithNullStatus() throws Exception {
        testTask.setTaskStatus(null);
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTask() throws Exception {

        var newTestUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newTestUser);

        var newTestTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTestTaskStatus);

        var newTaskModel = Instancio.of(modelGenerator.getTaskModel()).create();

        newTaskModel.setAssignee(newTestUser);
        newTaskModel.setTaskStatus(newTestTaskStatus);
        newTaskModel.setLabels(Set.of(generatedTestLabel(), generatedTestLabel()));
        var dto = mapper.mapToCreateDTO(newTaskModel);

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(
                testTask.getId()).orElseThrow();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(dto.getName());
        assertThat(task.getIndex()).isEqualTo(dto.getIndex());
        assertThat(task.getDescription()).isEqualTo(dto.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
        assertThat(task.getAssignee().getId()).isEqualTo(dto.getAssigneeId());
        assertThat(task.getLabels().stream().map(Label::getId).collect(Collectors.toSet()))
                .isEqualTo(dto.getTaskLabelIds());
    }

    @Test
    public void testPartialUpdateTask() throws Exception {

        var newTestUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newTestUser);

        var newTestTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTestTaskStatus);

        var dto = mapper.mapToCreateDTO(testTask);
        dto.setAssigneeId(newTestUser.getId());
        dto.setStatus(newTestTaskStatus.getSlug());

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(
                testTask.getId()).orElseThrow();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
        assertThat(task.getAssignee().getId()).isEqualTo(dto.getAssigneeId());
        assertThat(task.getLabels().stream().map(Label::getId).collect(Collectors.toSet()))
                .isEqualTo(testTask.getLabels().stream().map(Label::getId).collect(Collectors.toSet()));
    }

    @Test
    public void testUpdateTaskWithNotValidName() throws Exception {

        testTask.setName("");
        var dto = mapper.mapToCreateDTO(testTask);

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTaskWithNotPresentedStatus() throws Exception {

        var dto = mapper.mapToCreateDTO(testTask);
        dto.setStatus("Any_status");

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTaskWithNotPresentedLabel() throws Exception {

        var dto = mapper.mapToCreateDTO(testTask);
        dto.setTaskLabelIds(Set.of(-1L));

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTaskWithNotPresentedUser() throws Exception {

        var dto = mapper.mapToCreateDTO(testTask);
        dto.setAssigneeId(-1L);

        var request = put(url + "/{id}", testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDestroyTask() throws Exception {

        var request = delete(url + "/{id}", testTask.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var task = taskRepository.findById(
                testTask.getId()).orElse(null);

        assertThat(task).isNull();
    }

    @Test
    public void testFilteringWithTitleCont() throws Exception {

        var testTaskTitle = testTask.getName();

        var result = mockMvc.perform(get(url + "?titleCont=" + testTaskTitle).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("title").asString().containsIgnoringCase(testTaskTitle)));
    }

    @Test
    public void testFilteringWithAssigneeId() throws Exception {

        var testTaskAssigneeId = testTask.getAssignee().getId();
        var result = mockMvc.perform(get(url + "?assigneeId=" + testTaskAssigneeId).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("assignee_id").isEqualTo(testTaskAssigneeId)));
    }

    @Test
    public void testFilteringWithStatus() throws Exception {

        var testTaskStatusSlug = testTask.getTaskStatus().getSlug();
        var result = mockMvc.perform(get(url + "?status=" + testTaskStatusSlug).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("status").asString().containsIgnoringCase(testTaskStatusSlug)));
    }

    @Test
    public void testFilteringWithLabelId() throws Exception {

        var testLabelId = testTask.getLabels().stream().map(Label::getId).findFirst().orElse(1L);
        var result = mockMvc.perform(get(url + "?labelId=" + testLabelId).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("taskLabelIds").isArray().contains(testLabelId)));
    }

    @Test
    public void testFilteringWithAllParameters() throws Exception {

        var testTaskTitle = testTask.getName();
        var testTaskAssigneeId = testTask.getAssignee().getId();
        var testTaskStatusSlug = testTask.getTaskStatus().getSlug();
        var testLabelId = testTask.getLabels().stream().map(Label::getId).findFirst().orElse(1L);

        var result = mockMvc.perform(get(url + "?titleCont=" + testTaskTitle + "&assigneeId="
                        + testTaskAssigneeId + "&status=" + testTaskStatusSlug + "&labelId=" + testLabelId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("title").asString().containsIgnoringCase(testTaskTitle)));
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("assignee_id").isEqualTo(testTaskAssigneeId)));
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("status").asString().containsIgnoringCase(testTaskStatusSlug)));
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("taskLabelIds").isArray().contains(testLabelId)));
    }
}
