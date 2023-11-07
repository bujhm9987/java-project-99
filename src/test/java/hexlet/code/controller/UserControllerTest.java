package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
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

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
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
    private ObjectMapper om;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private Faker faker;

    private User testUser;

    @Value("${base-url}")
    @Autowired
    private String baseUrl;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get(baseUrl + "/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {

        userRepository.save(testUser);

        var request = get(baseUrl + "/users/{id}", testUser.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt()),
                v -> v.node("updatedAt").isEqualTo(testUser.getUpdatedAt())
        );
    }

    @Test
    public void testCreate() throws Exception {

        var dto = mapper.mapToCreateDTO(testUser);

        var request = post(baseUrl + "/users")
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
        var dto = mapper.mapToCreateDTO(testUser);
        dto.setEmail("email");

        var request = post(baseUrl + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNotValidPassword() throws Exception {
        var dto = mapper.mapToCreateDTO(testUser);
        dto.setPassword("pa");

        var request = post(baseUrl + "/users")
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

        var request = put(baseUrl + "/users/{id}", testUser.getId())
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

        var request = put(baseUrl + "/users/{id}", testUser.getId())
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
    public void testWithNotValidEmail() throws Exception {

        userRepository.save(testUser);

        var newData = Map.of(
                "email", "incorrect_email"
        );

        var request = put(baseUrl + "/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testWithNotValidPassword() throws Exception {

        userRepository.save(testUser);

        var newData = Map.of(
                "password", "pa"
        );

        var request = put(baseUrl + "/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    public void testDestroy() throws Exception {
        userRepository.save(testUser);
        var request = delete(baseUrl + "/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(false);
    }
}
