package com.lexora.lexora_backend.client;

import com.lexora.lexora_backend.tenant.Tenant;
import com.lexora.lexora_backend.tenant.TenantContext;
import com.lexora.lexora_backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;

    public ClientService(ClientRepository clientRepository,
                         TenantRepository tenantRepository) {
        this.clientRepository = clientRepository;
        this.tenantRepository = tenantRepository;
    }

    public ClientResponse createClient(CreateClientRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant not found"));

        Client client = new Client();
        client.setTenant(tenant);
        client.setFullName(request.fullName());
        client.setPhone(request.phone());
        client.setEmail(request.email());
        client.setAddress(request.address());

        return ClientResponse.from(clientRepository.save(client));
    }

    public List<ClientResponse> getAllClients() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        return clientRepository.findAllByTenantIdAndIsActive(tenantId, true)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    public ClientResponse getClient(UUID clientId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client not found"));

        return ClientResponse.from(client);
    }

    public ClientResponse updateClient(UUID clientId, UpdateClientRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client not found"));

        if (request.fullName() != null) client.setFullName(request.fullName());
        if (request.phone() != null) client.setPhone(request.phone());
        if (request.email() != null) client.setEmail(request.email());
        if (request.address() != null) client.setAddress(request.address());

        return ClientResponse.from(clientRepository.save(client));
    }

    public void deleteClient(UUID clientId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client not found"));

        client.setActive(false);
        clientRepository.save(client);
    }
}