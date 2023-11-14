package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.User;
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

import java.text.SimpleDateFormat;
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

    @Value("${base-url}" + "/tasks")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        userRepository.save(testUser);
        taskStatusRepository.save(testTaskStatus);

        var user = userRepository.findById(testUser.getId()).get();
        var taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().positive())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .create();
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
        var dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isEqualTo(dateFormatter.format(testTask.getCreatedAt()))
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
                "content", faker.lorem().sentence()
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
                .andExpect(status().isNotFound());
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
}
