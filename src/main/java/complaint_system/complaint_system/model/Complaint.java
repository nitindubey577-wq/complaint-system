package complaint_system.complaint_system.model;

public class Complaint {

    private String id;
    private String title;
    private String description;
    private String location;
    private String category;
    private ComplaintStatus status;
    private String departmentId;
    private String assignedOfficerId;

    public Complaint() {
    }

    public Complaint(
            String id,
            String title,
            String description,
            String category,
            ComplaintStatus status) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(String assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
}
