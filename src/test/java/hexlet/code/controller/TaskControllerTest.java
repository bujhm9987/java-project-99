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
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    private Task testTask;

    private TaskStatus taskStatus;

    private List<Label> testLabelList = new ArrayList<>();

    @Value("${base-url}" + "/tasks")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        testLabelList.add(generatedTestLabel());
        testLabelList.add(generatedTestLabel());

        userRepository.save(testUser);
        taskStatusRepository.save(testTaskStatus);

        var user = userRepository.findById(testUser.getId()).get();
        taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().positive())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .supply(Select.field(Task::getTaskLabels), () -> testLabelList)
                .create();
    }

    private Label generatedTestLabel() {
        var testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
        return labelRepository.findByName(
                testLabel.getName()).orElse(null);
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);

        var result = mockMvc.perform(get(url).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithoutAuthentication() throws Exception {
        taskRepository.save(testTask);

        mockMvc.perform(get(url))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(testTask);

        var request = get(url + "/{id}", testTask.getId()).with(token);
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
                v -> v.node("taskLabelIds").isEqualTo(testTask.getTaskLabels().stream().map(Label::getId).toList())
        );
    }

    @Test
    public void testShowWithoutAuthentication() throws Exception {
        taskRepository.save(testTask);

        var request = get(url + "/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testCreate() throws Exception {
        testTask.setName("Unique testName");
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(
                "Unique testName").orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTask.getTaskStatus().getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(testTask.getAssignee().getId());
        assertThat(task.getTaskLabels().get(0).getId()).isEqualTo(testTask.getTaskLabels().get(0).getId());
        assertThat(task.getTaskLabels().get(1).getId()).isEqualTo(testTask.getTaskLabels().get(1).getId());
    }

    @Test
    public void testCreateWithoutAuthentication() throws Exception {
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var task = taskRepository.findByName(
                testTask.getName()).orElse(null);

        assertThat(task).isNull();
    }

    @Test
    public void testCreateWithNotValidName() throws Exception {
        testTask.setName("");
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNullStatus() throws Exception {
        testTask.setTaskStatus(null);
        var dto = mapper.mapToCreateDTO(testTask);

        var request = post(url).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "index", faker.number().positive(),
                "assignee_id", userRepository.findByEmail("hexlet@example.com").get().getId(),
                "title", faker.lorem().word(),
                "status", taskStatusRepository.findBySlug("to_review").get().getSlug(),
                "content", faker.lorem().sentence(),
                "taskLabelIds", labelRepository.findById(1L).stream().map(Label::getId).toList()
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(
                testTask.getId()).orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(newData.get("title"));
        assertThat(task.getIndex()).isEqualTo(newData.get("index"));
        assertThat(task.getDescription()).isEqualTo(newData.get("content"));
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(newData.get("status"));
        assertThat(task.getAssignee().getId()).isEqualTo(newData.get("assignee_id"));
        assertThat(task.getTaskLabels().stream().map(Label::getId).toList()).isEqualTo(newData.get("taskLabelIds"));
    }

    @Test
    public void testUpdateWithoutAuthentication() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "index", faker.number().positive(),
                "content", faker.lorem().sentence()
        );

        var request = put(url + "/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "assignee_id", userRepository.findByEmail("hexlet@example.com").get().getId(),
                "status", taskStatusRepository.findBySlug("to_review").get().getSlug()
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(
                testTask.getId()).orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(newData.get("status"));
        assertThat(task.getAssignee().getId()).isEqualTo(newData.get("assignee_id"));
        assertThat(task.getTaskLabels().size()).isEqualTo(testTask.getTaskLabels().size());
    }

    @Test
    public void testUpdateWithNotValidName() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "title", ""
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotPresentedStatus() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "status", "Any_status"
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotPresentedLabel() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "taskLabelIds", "-1"
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotPresentedUser() throws Exception {
        taskRepository.save(testTask);

        var newData = Map.of(
                "assignee_id", "-1"
        );

        var request = put(url + "/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDestroy() throws Exception {
        taskRepository.save(testTask);

        var request = delete(url + "/{id}", testTask.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var task = taskRepository.findById(
                testTask.getId()).orElse(null);

        assertThat(task).isNull();
    }

    @Test
    public void testDestroyWithoutAuthentication() throws Exception {
        taskRepository.save(testTask);

        var request = delete(url + "/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var task = taskRepository.findById(
                testTask.getId()).orElse(null);

        assertThat(task).isNotNull();
    }

    @Test
    public void testFilteringWithTitleCont() throws Exception {
        taskRepository.save(testTask);
        var testTaskTitle = testTask.getName();

        var result = mockMvc.perform(get(url + "?titleCont=" + testTaskTitle).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("title").asString().containsIgnoringCase(testTaskTitle)));
    }

    @Test
    public void testFilteringWithAssigneeId() throws Exception {
        taskRepository.save(testTask);
        var testTaskAssigneeId = testTask.getAssignee().getId();
        var result = mockMvc.perform(get(url + "?assigneeId=" + testTaskAssigneeId).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("assignee_id").isEqualTo(testTaskAssigneeId)));
    }

    @Test
    public void testFilteringWithStatus() throws Exception {
        taskRepository.save(testTask);
        var testTaskStatusSlug = testTask.getTaskStatus().getSlug();
        var result = mockMvc.perform(get(url + "?status=" + testTaskStatusSlug).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("status").asString().containsIgnoringCase(testTaskStatusSlug)));
    }

    @Test
    public void testFilteringWithLabelId() throws Exception {
        taskRepository.save(testTask);
        var testTaskLabelId = testTask.getTaskLabels().get(0).getId();
        var result = mockMvc.perform(get(url + "?labelId=" + testTaskLabelId).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                assertThatJson(element)
                        .and(v -> v.node("taskLabelIds").isArray().contains(testTaskLabelId)));
    }

    @Test
    public void testFilteringWithAllParameters() throws Exception {
        taskRepository.save(testTask);
        var testTaskTitle = testTask.getName();
        var testTaskAssigneeId = testTask.getAssignee().getId();
        var testTaskStatusSlug = testTask.getTaskStatus().getSlug();
        var testTaskLabelId = testTask.getTaskLabels().get(0).getId();
        var result = mockMvc.perform(get(url + "?titleCont=" + testTaskTitle + "&assigneeId="
                        + testTaskAssigneeId + "&status=" + testTaskStatusSlug + "&labelId=" + testTaskLabelId)
                        .with(token))
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
                        .and(v -> v.node("taskLabelIds").isArray().contains(testTaskLabelId)));
    }
}
