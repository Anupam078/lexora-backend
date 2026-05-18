package com.lexora.lexora_backend.auth;

import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.tenant.TenantRepository;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(TenantRepository tenantRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ─── REGISTER ───────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // 1. Check subdomain isn't already taken
        if (tenantRepository.existsBySubdomain(request.subdomain())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Subdomain already taken");
        }

        // 2. Create and save the tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.tenantName());
        tenant.setSubdomain(request.subdomain());

        Tenant savedTenant = tenantRepository.save(tenant);

        // 3. Create the first admin user for this tenant
        User admin = new User();
        admin.setTenant(savedTenant);
        admin.setFullName(request.fullName());
        admin.setEmail(request.email());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setRole("ADMIN");

        userRepository.save(admin);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Tenant registered successfully");
    }

    // ─── LOGIN ───────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Find tenant by subdomain
        Tenant tenant = tenantRepository.findBySubdomain(request.subdomain())
                .orElse(null);

        if (tenant == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        // 2. Find user by email within that tenant
        User user = userRepository
                .findByEmailAndTenantId(request.email(), tenant.getId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        // 4. Generate and return JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                tenant.getId().toString(),
                user.getRole()
        );

        return ResponseEntity.ok(new LoginResponse(token));
    }
}