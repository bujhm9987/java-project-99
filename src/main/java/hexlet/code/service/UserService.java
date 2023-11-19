package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.AccessUserDeniedException;
import hexlet.code.exception.ConstraintViolationException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserUtils userUtils;


    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO create(UserCreateDTO userData) {
        var userDataEmail = userData.getEmail();
        var findUser = userRepository.findByEmail(userDataEmail);
        if (findUser.isPresent()) {
            throw new ConstraintViolationException(String.format("user with email %s already exists", userDataEmail));
        }
        var user = userMapper.map(userData);
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));
        return userMapper.map(user);
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));
        if (userUtils.getCurrentUser().getId() != id) {
            throw new AccessUserDeniedException("You do not have enough privileges to update this user");
        }
        var userDataEmail = userData.getEmail();
        if (userDataEmail != null) {
            var findUser = userRepository.findByEmail(userDataEmail.get());
            if (findUser.isPresent()) {
                throw new ConstraintViolationException(String.format("User with email %s already exists", userDataEmail));
            }
        }
        userMapper.update(userData, user);
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.map(user);
    }

    public void delete(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));
        if (userUtils.getCurrentUser().getId() != id) {
            throw new AccessUserDeniedException("You do not have enough privileges to update this user");
        }
        var userTasks = user.getTasks();
        if (userTasks.isEmpty()) {
            userRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("User with id %s has active tasks", id));
        }
    }
}
