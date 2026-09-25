package complaint_system.complaint_system.dto;

import complaint_system.complaint_system.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {
    @NotBlank(message = "Name is required") private String name;
    @Email(message = "Email must be valid") @NotBlank(message = "Email is required") private String email;
    @NotBlank(message = "Password is required") private String password;
    private String phone;
    private String departmentId;
    private UserRole role = UserRole.CITIZEN;
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getDepartmentId() { return departmentId; } public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public UserRole getRole() { return role; } public void setRole(UserRole role) { this.role = role; }
}
