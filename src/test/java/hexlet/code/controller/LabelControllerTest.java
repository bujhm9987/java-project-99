package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
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
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private Label testLabel;

    @Value("${base-url}" + "/labels")
    @Autowired
    private String url;

    @BeforeEach
    public void setUp() {
        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        labelRepository.save(testLabel);

        var result = mockMvc.perform(get(url).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    /*@Test
    public void testIndexWithoutAuthentication() throws Exception {
        labelRepository.save(testLabel);

        var result = mockMvc.perform(get(url))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }*/

    @Test
    public void testShow() throws Exception {
        labelRepository.save(testLabel);

        var request = get(url + "/{id}", testLabel.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );
    }

    /*@Test
    public void testShowWithoutAuthentication() throws Exception {
        labelRepository.save(testLabel);

        var request = get(url + "/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
    }*/

    @Test
    public void testCreate() throws Exception {
        testLabel.setName("Unique name");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var label = labelRepository.findByName(
                testLabel.getName()).orElseThrow();

        assertThat(label).isNotNull();
        assertThat(label.getName()).isEqualTo(testLabel.getName());
    }

    @Test
    public void testCreateWithNotValidName() throws Exception {
        testLabel.setName("Un");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWhitAlreadyExistName() throws Exception {
        testLabel.setName("bug");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = post(url).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    /*@Test
    public void testCreateWithAuthentication() throws Exception {
        testLabel.setName("Unique name");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }*/

    @Test
    public void testUpdate() throws Exception {
        labelRepository.save(testLabel);

        testLabel.setName("New name");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = put(url + "/{id}", testLabel.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var label = labelRepository.findById(
                testLabel.getId()).orElseThrow();

        assertThat(label).isNotNull();
        assertThat(label.getName()).isEqualTo(dto.getName());
    }

    @Test
    public void testUpdateWithNotValidName() throws Exception {
        labelRepository.save(testLabel);

        testLabel.setName("U");
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = put(url + "/{id}", testLabel.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWhitAlreadyExistName() throws Exception {
        labelRepository.save(testLabel);
        var testLabelName = testLabel.getName();

        testLabel.setName(testLabelName);
        var dto = mapper.mapToCreateDTO(testLabel);

        var request = put(url + "/{id}", testLabel.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    /*@Test
    public void testUpdateWithoutAuthentication() throws Exception {
        labelRepository.save(testLabel);

        var newData = Map.of(
                "name", faker.lorem().characters(3, 1000)
        );

        var request = put(url + "/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }*/

    @Test
    public void testDestroy() throws Exception {
        labelRepository.save(testLabel);
        var request = delete(url + "/{id}", testLabel.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var label = labelRepository.findById(
                testLabel.getId()).orElse(null);

        assertThat(label).isNull();
    }

    /*@Test
    public void testDestroyWithoutAuthentication() throws Exception {
        labelRepository.save(testLabel);
        var request = delete(url + "/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        var label = labelRepository.findById(
                testLabel.getId()).orElse(null);

        assertThat(label).isNotNull();
    }*/

    /*@Test
    public void testDestroyWithActiveTask() throws Exception {
        labelRepository.save(testLabel);
        var testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);

        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        var testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setLabels(Set.of(testLabel));
        taskRepository.save(testTask);

        var request = delete(url + "/{id}", testLabel.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var label = labelRepository.findById(
                testLabel.getId()).orElse(null);

        assertThat(label).isNotNull();
    }*/
}
