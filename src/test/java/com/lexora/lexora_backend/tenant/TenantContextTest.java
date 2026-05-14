package com.lexora.lexora_backend.tenant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void shouldSetAndGetTenantId() {
        TenantContext.setTenantId("delhi-legal-aid");
        assertEquals("delhi-legal-aid", TenantContext.getTenantId());
        TenantContext.clear();
    }

    @Test
    void shouldClearTenantId() {
        TenantContext.setTenantId("mumbai-district-court");
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void shouldBeNullByDefault() {
        assertNull(TenantContext.getTenantId());
    }
}