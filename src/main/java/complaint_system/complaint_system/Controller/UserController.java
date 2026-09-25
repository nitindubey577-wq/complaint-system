package complaint_system.complaint_system.Controller;

import complaint_system.complaint_system.dto.CreateUserRequest;
import complaint_system.complaint_system.model.User;
import complaint_system.complaint_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @GetMapping public List<User> all() { return service.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<User> one(@PathVariable String id) { return ResponseEntity.of(java.util.Optional.ofNullable(service.findById(id))); }
    @PostMapping public ResponseEntity<User> create(@Valid @RequestBody CreateUserRequest request) { return ResponseEntity.status(201).body(service.create(request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable String id) { return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); }
}
