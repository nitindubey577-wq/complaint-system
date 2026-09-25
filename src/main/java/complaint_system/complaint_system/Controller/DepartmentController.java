package complaint_system.complaint_system.Controller;

import complaint_system.complaint_system.dto.DepartmentRequest;
import complaint_system.complaint_system.model.Department;
import complaint_system.complaint_system.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService service;
    public DepartmentController(DepartmentService service) { this.service = service; }
    @GetMapping public List<Department> all() { return service.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<Department> one(@PathVariable String id) { return ResponseEntity.of(java.util.Optional.ofNullable(service.findById(id))); }
    @PostMapping public ResponseEntity<Department> create(@Valid @RequestBody DepartmentRequest request) { return ResponseEntity.status(201).body(service.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<Department> update(@PathVariable String id, @Valid @RequestBody DepartmentRequest request) { return ResponseEntity.of(java.util.Optional.ofNullable(service.update(id, request))); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable String id) { return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); }
}
