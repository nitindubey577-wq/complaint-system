package complaint_system.complaint_system.Controller;

import complaint_system.complaint_system.dto.CreateComplaintRequest;
import complaint_system.complaint_system.dto.UpdateComplaintRequest;
import complaint_system.complaint_system.model.Complaint;
import complaint_system.complaint_system.model.ComplaintHistory;
import complaint_system.complaint_system.model.ComplaintStatus;
import complaint_system.complaint_system.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public List<Complaint> getAllComplaints(
            @RequestParam(required = false) String status) {

        return complaintService.getAllComplaints(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable String id) {
        Complaint complaint = complaintService.getComplaintById(id);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request) {

        Complaint complaint = complaintService.createComplaint(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Complaint> updateComplaint(
            @PathVariable String id,
            @Valid @RequestBody UpdateComplaintRequest request) {

        Complaint complaint = complaintService.updateComplaint(id, request);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Complaint> updateComplaintStatus(
            @PathVariable String id,
            @RequestParam ComplaintStatus newStatus,
            @RequestParam(required = false) String remark) {

        Complaint complaint = complaintService.updateComplaintStatus(
                id,
                newStatus,
                remark
        );

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Complaint> assignComplaint(
            @PathVariable String id,
            @RequestParam String departmentId,
            @RequestParam(required = false) String officerId,
            @RequestParam(required = false) String remark) {
        Complaint complaint = complaintService.assignComplaint(id, departmentId, officerId, remark);
        if (complaint == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ComplaintHistory>> getComplaintHistory(
            @PathVariable String id) {

        List<ComplaintHistory> history = complaintService.getComplaintHistory(id);

        if (history == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(history);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable String id) {
        boolean deleted = complaintService.deleteComplaint(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
