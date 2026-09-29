package complaint_system.complaint_system.service;

import complaint_system.complaint_system.dto.CreateComplaintRequest;
import complaint_system.complaint_system.dto.UpdateComplaintRequest;
import complaint_system.complaint_system.model.Complaint;
import complaint_system.complaint_system.model.ComplaintHistory;
import complaint_system.complaint_system.model.ComplaintStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComplaintService {

    private final List<Complaint> complaints = new ArrayList<>();
    private final List<ComplaintHistory> complaintHistories = new ArrayList<>();

    private long nextId = 3;
    private long nextHistoryId = 1;

    public ComplaintService() {
        complaints.add(new Complaint(
                "1",
                "Street light not working",
                "The street light near my house is not working.",
                "ELECTRICITY",
                ComplaintStatus.PENDING
        ));

        complaints.add(new Complaint(
                "2",
                "Water pipe leaking",
                "Water is leaking near the main road.",
                "WATER",
                ComplaintStatus.IN_PROGRESS
        ));
    }

    public List<Complaint> getAllComplaints(String status) {
        if (status == null || status.isBlank()) {
            return complaints;
        }

        List<Complaint> filteredComplaints = new ArrayList<>();

        for (Complaint complaint : complaints) {
            if (complaint.getStatus().name().equalsIgnoreCase(status)) {
                filteredComplaints.add(complaint);
            }
        }

        return filteredComplaints;
    }

    public Complaint getComplaintById(String id) {
        for (Complaint complaint : complaints) {
            if (complaint.getId().equals(id)) {
                return complaint;
            }
        }

        return null;
    }

    public Complaint createComplaint(CreateComplaintRequest request) {
        Complaint complaint = new Complaint();

        complaint.setId(String.valueOf(nextId));
        nextId++;

        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLocation(request.getLocation());
        complaint.setCategory(request.getCategory());
        complaint.setDepartmentId(request.getDepartmentId());
        complaint.setStatus(ComplaintStatus.PENDING);

        complaints.add(complaint);

        return complaint;
    }

    public Complaint updateComplaint(String id, UpdateComplaintRequest request) {
        Complaint complaint = getComplaintById(id);

        if (complaint == null) {
            return null;
        }

        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());

        return complaint;
    }

    public boolean deleteComplaint(String id) {
        for (int index = 0; index < complaints.size(); index++) {
            if (complaints.get(index).getId().equals(id)) {
                complaints.remove(index);
                return true;
            }
        }

        return false;
    }

    public Complaint updateComplaintStatus(
            String id,
            ComplaintStatus newStatus,
            String remark) {

        Complaint complaint = getComplaintById(id);

        if (complaint == null) {
            return null;
        }

        if (!isValidStatusTransition(complaint.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + complaint.getStatus()
                            + " to "
                            + newStatus
            );
        }

        ComplaintHistory history = new ComplaintHistory(
                String.valueOf(nextHistoryId),
                complaint.getId(),
                complaint.getStatus(),
                newStatus,
                remark,
                LocalDateTime.now()
        );

        nextHistoryId++;
        complaintHistories.add(history);

        complaint.setStatus(newStatus);

        return complaint;
    }

    public List<ComplaintHistory> getComplaintHistory(String complaintId) {
        if (getComplaintById(complaintId) == null) {
            return null;
        }

        List<ComplaintHistory> historyForComplaint = new ArrayList<>();

        for (ComplaintHistory history : complaintHistories) {
            if (history.getComplaintId().equals(complaintId)) {
                historyForComplaint.add(history);
            }
        }

        return historyForComplaint;
    }

    public Complaint assignComplaint(String id, String departmentId, String officerId, String remark) {
        Complaint complaint = getComplaintById(id);
        if (complaint == null) return null;
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING complaints can be assigned");
        }
        complaint.setDepartmentId(departmentId);
        complaint.setAssignedOfficerId(officerId);
        return updateComplaintStatus(id, ComplaintStatus.ASSIGNED, remark == null ? "Complaint assigned" : remark);
    }

    private boolean isValidStatusTransition(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus) {

        switch (currentStatus) {
            case PENDING:
                return newStatus == ComplaintStatus.ASSIGNED
                        || newStatus == ComplaintStatus.REJECTED;

            case ASSIGNED:
                return newStatus == ComplaintStatus.IN_PROGRESS;

            case IN_PROGRESS:
                return newStatus == ComplaintStatus.RESOLVED;

            case RESOLVED:
                return newStatus == ComplaintStatus.CLOSED;

            default:
                return false;
        }
    }
}
