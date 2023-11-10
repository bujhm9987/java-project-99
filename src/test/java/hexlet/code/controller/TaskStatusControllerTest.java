package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import org.springframework.test.web.servlet.MockMvc;

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
class TaskStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;


    private User testUser;

    private TaskStatus testTaskStatus;

    @Value("${base-url}")
    @Autowired
    private String baseUrl;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
    }

    @Test
    public void testIndex() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var result = mockMvc.perform(get(baseUrl + "/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = get(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(testTaskStatus.getCreatedAt())
        );
    }

    @Test
    public void testCreate() throws Exception {

        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(baseUrl + "/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(
                testTaskStatus.getSlug()).orElse(null);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    public void testCreateWithoutAuthentication() throws Exception {

        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(baseUrl + "/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateWithNotValidName() throws Exception {
        testTaskStatus.setName("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(baseUrl + "/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNotValidSlug() throws Exception {
        testTaskStatus.setSlug("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(baseUrl + "/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "name", faker.name().name(),
                "slug", faker.internet().slug()
        );

        var request = put(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(newData.get("name"));
        assertThat(taskStatus.getSlug()).isEqualTo(newData.get("slug"));
    }

    @Test
    public void testPartialUpdate() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "slug", faker.internet().slug()
        );

        var request = put(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(newData.get("slug"));
    }

    @Test
    public void testUpdateWithNotValidName() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "name", ""
        );

        var request = put(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotValidPassword() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "slug", ""
        );

        var request = put(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithoutAuthentication() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "name", faker.name().name(),
                "slug", faker.internet().slug()
        );

        var request = put(baseUrl + "/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroy() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = delete(baseUrl + "/task_statuses/{id}", testTaskStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNull();
    }

    @Test
    public void testDestroyWithoutAuthentication() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = delete(baseUrl + "/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNotNull();
    }
}
