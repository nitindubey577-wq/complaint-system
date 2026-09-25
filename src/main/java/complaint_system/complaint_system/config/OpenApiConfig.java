package complaint_system.complaint_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI complaintManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Complaint Management System API")
                .version("1.0.0")
                .description("In-memory REST API for citizen complaints, departments, users, and complaint workflow."));
    }
}
