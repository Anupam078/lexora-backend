package com.lexora.lexora_backend.client;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findAllByTenantIdAndIsActive(UUID tenantId, boolean isActive);
    Optional<Client> findByIdAndTenantId(UUID id, UUID tenantId);
}