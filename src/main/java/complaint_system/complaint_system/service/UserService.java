package complaint_system.complaint_system.service;

import complaint_system.complaint_system.dto.CreateUserRequest;
import complaint_system.complaint_system.model.User;
import complaint_system.complaint_system.model.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final List<User> users = new ArrayList<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private long nextId = 3;
    public UserService() {
        users.add(new User("1", "System Admin", "admin@complaints.local", passwordEncoder.encode("admin123"), "9999999999", UserRole.ADMIN, LocalDateTime.now()));
        users.add(new User("2", "Rahul Officer", "rahul@complaints.local", passwordEncoder.encode("officer123"), "8888888888", UserRole.OFFICER, LocalDateTime.now()));
        users.get(1).setDepartmentId("1");
    }
    public List<User> findAll() { return users; }
    public User findById(String id) { return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null); }
    public User findByEmail(String email) { return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null); }
    public User create(CreateUserRequest request) {
        if (findByEmail(request.getEmail()) != null) throw new IllegalArgumentException("Email is already registered");
        User user = new User(String.valueOf(nextId++), request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getPhone(), request.getRole(), LocalDateTime.now());
        user.setDepartmentId(request.getDepartmentId());
        users.add(user); return user;
    }
    public boolean delete(String id) { return users.removeIf(u -> u.getId().equals(id)); }
}
