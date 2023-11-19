package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.UserMapper;
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
class UserControllerTest {
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
    private UserMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tokenDefaultUser;

    private User testUser;

    @Value("${base-url}" + "/users")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        tokenDefaultUser = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get(url).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithoutAuthentication() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get(url))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testShow() throws Exception {

        userRepository.save(testUser);

        var request = get(url + "/{id}", testUser.getId()).with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail())
        );
    }

    @Test
    public void testShowWithoutAuthentication() throws Exception {
        userRepository.save(testUser);

        var request = get(url + "/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testCreate() throws Exception {

        var dto = mapper.mapToCreateDTO(testUser);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(
                testUser.getEmail()).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    public void testCreateWithNotValidEmail() throws Exception {
        testUser.setEmail("email");
        var dto = mapper.mapToCreateDTO(testUser);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNotValidPassword() throws Exception {
        testUser.setPassword("pa");
        var dto = mapper.mapToCreateDTO(testUser);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        userRepository.save(testUser);

        var newData = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put(url + "/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findById(
                testUser.getId()).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(newData.get("firstName"));
        assertThat(user.getLastName()).isEqualTo(newData.get("lastName"));
        assertThat(user.getEmail()).isEqualTo(newData.get("email"));
        assertThat(user.getPassword()).isNotEqualTo(newData.get("password"));
    }

    @Test
    public void testPartialUpdate() throws Exception {

        userRepository.save(testUser);

        var newData = Map.of(
                "firstName", faker.name().firstName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put(url + "/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findById(
                testUser.getId()).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(newData.get("firstName"));
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(newData.get("password"));
    }

    @Test
    public void testUpdateWithNotValidEmail() throws Exception {

        userRepository.save(testUser);

        var newData = Map.of(
                "email", "incorrect_email"
        );

        var request = put(url + "/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithNotValidPassword() throws Exception {

        userRepository.save(testUser);

        var newData = Map.of(
                "password", "pa"
        );

        var request = put(url + "/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithoutAuthentication() throws Exception {
        userRepository.save(testUser);

        var newData = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put(url + "/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateAnotherUser() throws Exception {
        userRepository.save(testUser);

        var newData = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put(url + "/{id}", testUser.getId()).with(tokenDefaultUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDestroy() throws Exception {
        userRepository.save(testUser);
        var request = delete(url + "/{id}", testUser.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var user = userRepository.findById(
                testUser.getId()).orElse(null);

        assertThat(user).isNull();
    }

    @Test
    public void testDestroyWithoutAuthentication() throws Exception {
        userRepository.save(testUser);
        var request = delete(url + "/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var user = userRepository.findById(
                testUser.getId()).orElse(null);

        assertThat(user).isNotNull();
    }

    @Test
    public void testDestroyAnotherUser() throws Exception {
        userRepository.save(testUser);
        var request = delete(url + "/{id}", testUser.getId()).with(tokenDefaultUser);
        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findById(
                testUser.getId()).orElse(null);

        assertThat(user).isNotNull();
    }

    @Test
    public void testDestroyWithActiveTask() throws Exception {
        userRepository.save(testUser);

        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        var user = userRepository.findById(testUser.getId()).get();
        var taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();

        var testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().positive())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .ignore(Select.field(Task::getTaskLabels))
                .create();
        taskRepository.save(testTask);

        var request = delete(url + "/{id}", testUser.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var delUser = userRepository.findById(
                user.getId()).orElse(null);

        assertThat(delUser).isNotNull();
    }

}
