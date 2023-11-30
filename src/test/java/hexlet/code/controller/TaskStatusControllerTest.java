package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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

import org.springframework.test.web.servlet.MockMvc;

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
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private TaskStatus testTaskStatus;

    @Value("${base-url}" + "/task_statuses")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
    }

    @Test
    public void testIndex() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var result = mockMvc.perform(get(url).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = get(url + "/{id}", testTaskStatus.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {

        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(
                testTaskStatus.getSlug()).orElseThrow();

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    /*@Test
    public void testCreateWithoutAuthentication() throws Exception {

        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }*/

    @Test
    public void testCreateWithNotValidName() throws Exception {
        testTaskStatus.setName("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNotValidSlug() throws Exception {
        testTaskStatus.setSlug("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var newTaskStatusModel = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        var dto = mapper.mapToCreateDTO(newTaskStatusModel);

        var request = put(url + "/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElseThrow();

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(dto.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(dto.getSlug());
    }

    @Test
    public void testPartialUpdate() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        testTaskStatus.setSlug(faker.internet().slug());
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = put(url + "/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElseThrow();

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(dto.getSlug());
    }

    @Test
    public void testUpdateWithNotValidName() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        testTaskStatus.setName("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = put(url + "/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotValidSlug() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        testTaskStatus.setSlug("");
        var dto = mapper.mapToCreateDTO(testTaskStatus);

        var request = put(url + "/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    /*@Test
    public void testUpdateWithoutAuthentication() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var newData = Map.of(
                "name", faker.name().name(),
                "slug", faker.internet().slug()
        );

        var request = put(url + "/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }*/

    @Test
    public void testDestroy() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = delete(url + "/{id}", testTaskStatus.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNull();
    }

    /*@Test
    public void testDestroyWithoutAuthentication() throws Exception {

        taskStatusRepository.save(testTaskStatus);

        var request = delete(url + "/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNotNull();
    }*/

    /*@Test
    public void testDestroyWithActiveTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);

        var testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);

        var request = delete(url + "/{id}", testTaskStatus.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var taskStatus = taskStatusRepository.findById(
                testTaskStatus.getId()).orElse(null);

        assertThat(taskStatus).isNotNull();
    }*/
}
