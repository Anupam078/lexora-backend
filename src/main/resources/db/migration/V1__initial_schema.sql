-- Tenants (law firms / legal aid clinics)
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    subdomain VARCHAR UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP
);

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id),
    full_name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    password_hash VARCHAR NOT NULL,
    role VARCHAR NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    UNIQUE(tenant_id, email)
);

-- Cases
CREATE TABLE cases (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id),
    case_number VARCHAR NOT NULL,
    title VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    court_name VARCHAR NOT NULL,
    advocate_id UUID REFERENCES users(id),
    petitioner_name VARCHAR NOT NULL,
    respondent_name VARCHAR NOT NULL,
    filing_date DATE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, case_number)
);

-- Hearings
CREATE TABLE hearings (
    id UUID PRIMARY KEY,
    case_id UUID REFERENCES cases(id),
    tenant_id UUID,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME,
    court_room VARCHAR,
    purpose VARCHAR,
    status VARCHAR NOT NULL,
    adjournment_reason VARCHAR,
    next_date DATE,
    notes TEXT,
    created_at TIMESTAMP
);

-- Documents
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    case_id UUID REFERENCES cases(id),
    tenant_id UUID,
    file_name VARCHAR NOT NULL,
    file_key VARCHAR NOT NULL,
    file_type VARCHAR,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMP
);

-- Audit Log (append-only, never update or delete)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID,
    entity_type VARCHAR NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR NOT NULL,
    old_value TEXT,
    new_value TEXT,
    performed_by UUID REFERENCES users(id),
    performed_at TIMESTAMP NOT NULL
);

-- Clients
CREATE TABLE clients (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id),
    full_name VARCHAR NOT NULL,
    phone VARCHAR,
    email VARCHAR,
    address TEXT,
    created_at TIMESTAMP
);

-- Case-Client link
CREATE TABLE case_clients (
    case_id UUID REFERENCES cases(id),
    client_id UUID REFERENCES clients(id),
    PRIMARY KEY (case_id, client_id)
);