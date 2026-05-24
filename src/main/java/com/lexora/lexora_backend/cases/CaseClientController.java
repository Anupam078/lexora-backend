package com.lexora.lexora_backend.cases;

import com.lexora.lexora_backend.client.Client;
import com.lexora.lexora_backend.client.ClientRepository;
import com.lexora.lexora_backend.client.ClientResponse;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.user.Role;
import com.lexora.lexora_backend.user.User;
import com.lexora.lexora_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases/{caseId}/clients")
@PreAuthorize("hasRole('ADVOCATE') or hasRole('ADMIN')")
public class CaseClientController {

    private final CaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public CaseClientController(CaseRepository caseRepository,
                                ClientRepository clientRepository,
                                UserRepository userRepository) {
        this.caseRepository = caseRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Case getVerifiedCase(UUID caseId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        User currentUser = getCurrentUser();

        Case case_ = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        if (!case_.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        // Advocate can only manage their own cases
        if (currentUser.getRole() == Role.ADVOCATE &&
                !case_.getAdvocate().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return case_;
    }

    @PostMapping("/{clientId}")
    public ResponseEntity<String> linkClient(
            @PathVariable UUID caseId,
            @PathVariable UUID clientId) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Case case_ = getVerifiedCase(caseId);

        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client not found"));

        // Check not already linked
        boolean alreadyLinked = case_.getClients().stream()
                .anyMatch(c -> c.getId().equals(clientId));

        if (alreadyLinked) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Client already linked to this case");
        }

        case_.getClients().add(client);
        caseRepository.save(case_);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Client linked successfully");
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getClientsForCase(
            @PathVariable UUID caseId) {

        Case case_ = getVerifiedCase(caseId);

        List<ClientResponse> clients = case_.getClients()
                .stream()
                .filter(c -> c.isActive())
                .map(ClientResponse::from)
                .toList();

        return ResponseEntity.ok(clients);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> unlinkClient(
            @PathVariable UUID caseId,
            @PathVariable UUID clientId) {

        Case case_ = getVerifiedCase(caseId);

        boolean removed = case_.getClients()
                .removeIf(c -> c.getId().equals(clientId));

        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Client not linked to this case");
        }

        caseRepository.save(case_);
        return ResponseEntity.ok("Client unlinked successfully");
    }
}