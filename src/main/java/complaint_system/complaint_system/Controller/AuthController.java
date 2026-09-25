package complaint_system.complaint_system.Controller;
import complaint_system.complaint_system.dto.LoginRequest;
import complaint_system.complaint_system.model.User;
import complaint_system.complaint_system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager manager; private final UserService users;
    public AuthController(AuthenticationManager manager, UserService users){this.manager=manager;this.users=users;}
    @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        try { Authentication auth=manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(),request.getPassword())); HttpSession session=http.getSession(true); org.springframework.security.core.context.SecurityContext context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth); org.springframework.security.core.context.SecurityContextHolder.setContext(context); session.setAttribute("SPRING_SECURITY_CONTEXT",context); User user=users.findByEmail(request.getEmail()); return ResponseEntity.ok(Map.of("name",user.getName(),"email",user.getEmail(),"role",user.getRole())); } catch(Exception e){return ResponseEntity.status(401).body(Map.of("message","Invalid email or password"));}
    }
    @GetMapping("/me") public ResponseEntity<?> me(Authentication authentication){ if(authentication==null) return ResponseEntity.status(401).build(); User user=users.findByEmail(authentication.getName()); return ResponseEntity.ok(Map.of("name",user.getName(),"email",user.getEmail(),"role",user.getRole())); }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request){ HttpSession session=request.getSession(false); if(session!=null)session.invalidate(); return ResponseEntity.noContent().build(); }
}
