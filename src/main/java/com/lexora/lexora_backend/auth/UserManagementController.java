package com.lexora.lexora_backend.auth;

import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.tenant.TenantRepository;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementController(UserRepository userRepository,
                                    TenantRepository tenantRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createAdvocate(@RequestBody CreateAdvocateRequest request) {

        // 1. Get tenantId from JWT via TenantContext
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        // 2. Check email not already taken in this tenant
        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists in this tenant");
        }

        // 3. Load tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 4. Create advocate
        User advocate = new User();
        advocate.setTenant(tenant);
        advocate.setFullName(request.fullName());
        advocate.setEmail(request.email());
        advocate.setPasswordHash(passwordEncoder.encode(request.password()));
        advocate.setRole(Role.ADVOCATE);

        userRepository.save(advocate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Advocate created successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<User> users = userRepository.findAllByTenantId(tenantId);
        return ResponseEntity.ok(users);
    }
}