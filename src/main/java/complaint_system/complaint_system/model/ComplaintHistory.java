package complaint_system.complaint_system.model;

import java.time.LocalDateTime;

public class ComplaintHistory {

    private String id;
    private String complaintId;
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private String remark;
    private LocalDateTime createdAt;

    public ComplaintHistory() {
    }

    public ComplaintHistory(
            String id,
            String complaintId,
            ComplaintStatus oldStatus,
            ComplaintStatus newStatus,
            String remark,
            LocalDateTime createdAt) {

        this.id = id;
        this.complaintId = complaintId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public ComplaintStatus getOldStatus() {
        return oldStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public String getRemark() {
        return remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}