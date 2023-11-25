package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.AccessUserDeniedException;
import hexlet.code.exception.ConstraintViolationException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

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
                throw new ConstraintViolationException(
                        String.format("User with email %s already exists", userDataEmail.get()));
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

        var userTasks = taskRepository.findByAssigneeId(userUtils.getCurrentUser().getId());
        if (userTasks.isEmpty()) {
            userRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("User with id %s has active tasks", id));
        }
        /*var userTasks = user.getTasks();
        if (userTasks.isEmpty()) {
            userRepository.deleteById(id);
        } else {
            throw new ConstraintViolationException(String.format("User with id %s has active tasks", id));
        }*/
    }

    @Override
    public void createUser(UserDetails user) {
        var newUser = new User();
        newUser.setEmail(user.getUsername());
        newUser.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(newUser);
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public boolean userExists(String username) {
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
