package complaint_system.complaint_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String email;
    @JsonIgnore
    private String password;
    private String phone;
    private String departmentId;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() { }
    public User(String id, String name, String email, String password, String phone, UserRole role, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.email = email; this.password = password; this.phone = phone; this.role = role; this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
