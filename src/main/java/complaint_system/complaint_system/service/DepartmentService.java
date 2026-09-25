package complaint_system.complaint_system.service;

import complaint_system.complaint_system.dto.DepartmentRequest;
import complaint_system.complaint_system.model.Department;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentService {
    private final List<Department> departments = new ArrayList<>();
    private long nextId = 6;
    public DepartmentService() {
        departments.add(new Department("1", "Electricity Department", "Street lights and electricity", LocalDateTime.now()));
        departments.add(new Department("2", "Water Department", "Water supply and leakages", LocalDateTime.now()));
        departments.add(new Department("3", "Roads Department", "Potholes, damaged roads, and public infrastructure", LocalDateTime.now()));
        departments.add(new Department("4", "Sanitation Department", "Garbage collection, drainage, and cleanliness", LocalDateTime.now()));
        departments.add(new Department("5", "Public Safety Department", "Unsafe public spaces and safety concerns", LocalDateTime.now()));
    }
    public List<Department> findAll() { return departments; }
    public Department findById(String id) { return departments.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null); }
    public Department create(DepartmentRequest request) {
        Department department = new Department(String.valueOf(nextId++), request.getName(), request.getDescription(), LocalDateTime.now());
        departments.add(department); return department;
    }
    public Department update(String id, DepartmentRequest request) {
        Department department = findById(id); if (department == null) return null;
        department.setName(request.getName()); department.setDescription(request.getDescription()); return department;
    }
    public boolean delete(String id) { return departments.removeIf(d -> d.getId().equals(id)); }
}
