package com.lexora.lexora_backend.auth;

import com.lexora.lexora_backend.audit.AuditAction;
import com.lexora.lexora_backend.audit.AuditService;
import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.tenant.TenantRepository;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import com.lexora.lexora_backend.user.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserManagementController(UserRepository userRepository,
                                    TenantRepository tenantRepository,
                                    PasswordEncoder passwordEncoder,
                                    AuditService auditService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
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
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<UserResponse> users = userRepository.findAllByTenantId(tenantId)
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID userId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "User is already inactive");
        }

        user.setActive(false);
        userRepository.save(user);

        auditService.log("USER", user.getId(), AuditAction.UPDATED,
                "active", "inactive");

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/users/{userId}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable UUID userId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "User is already active");
        }

        user.setActive(true);
        userRepository.save(user);

        auditService.log("USER", user.getId(), AuditAction.UPDATED,
                "inactive", "active");

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // Prevent admin from deleting themselves
        String currentUserId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (user.getId().equals(UUID.fromString(currentUserId))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You cannot delete your own account");
        }

        user.setActive(false);
        userRepository.save(user);

        auditService.log("USER", user.getId(), AuditAction.DELETED,
                user.getEmail(), null);

        return ResponseEntity.ok("User removed successfully");
    }
}