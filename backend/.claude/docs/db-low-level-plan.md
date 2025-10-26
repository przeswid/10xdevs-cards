# PostgreSQL Database Schema - 10x-cards MVP

## 1. Tables with Columns, Data Types and Constraints

### users
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### user_roles
```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### ai_generation_sessions
```sql
CREATE TABLE ai_generation_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    input_text TEXT NOT NULL 
        CHECK (LENGTH(input_text) >= 1000 AND LENGTH(input_text) <= 10000),
    generated_count INTEGER NOT NULL DEFAULT 0,
    accepted_count INTEGER NOT NULL DEFAULT 0,
    ai_model VARCHAR(50),
    api_cost DECIMAL(10,4),
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL 
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### flashcards
```sql
CREATE TABLE flashcards (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    front_content VARCHAR(1000) NOT NULL,
    back_content VARCHAR(1000) NOT NULL,
    source VARCHAR(20) NOT NULL 
        CHECK (source IN ('AI', 'AI_USER', 'USER')),
    generation_session_id UUID,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (generation_session_id) REFERENCES ai_generation_sessions(id) ON DELETE SET NULL
);
```

## 2. Table Relationships

### Primary Relationships
- **users** → **flashcards**: One-to-Many (1:N)
  - One user can have multiple flashcards
  - Foreign key: `flashcards.user_id` → `users.id`
  - Cascade delete: When user is deleted, all their flashcards are deleted

- **users** → **ai_generation_sessions**: One-to-Many (1:N)
  - One user can have multiple AI generation sessions
  - Foreign key: `ai_generation_sessions.user_id` → `users.id`
  - Cascade delete: When user is deleted, all their AI sessions are deleted

- **users** → **user_roles**: One-to-Many (1:N)
  - One user can have multiple roles
  - Foreign key: `user_roles.user_id` → `users.id`
  - Cascade delete: When user is deleted, all their roles are deleted

- **ai_generation_sessions** → **flashcards**: One-to-Many (1:N, Optional)
  - One AI session can generate multiple flashcards
  - Foreign key: `flashcards.generation_session_id` → `ai_generation_sessions.id`
  - Set null on delete: When AI session is deleted, flashcards remain but lose session reference

### Cardinality Summary
- User : Flashcards = 1:N (mandatory on flashcard side)
- User : AI_Generation_Sessions = 1:N (mandatory on session side)
- User : User_Roles = 1:N (mandatory on role side)
- AI_Generation_Session : Flashcards = 1:N (optional on flashcard side)

## 3. Indexes

### Performance Indexes
```sql
-- User authentication and lookup
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- Flashcard queries by user
CREATE INDEX idx_flashcards_user_id ON flashcards(user_id);

-- AI session queries by user
CREATE INDEX idx_ai_generation_sessions_user_id ON ai_generation_sessions(user_id);

-- User roles lookup
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- Optional: Flashcard queries by generation session
CREATE INDEX idx_flashcards_generation_session_id ON flashcards(generation_session_id);
```

### Composite Indexes (Future Consideration)
```sql
-- For flashcard listing with sorting
-- CREATE INDEX idx_flashcards_user_created ON flashcards(user_id, created_at DESC);

-- For AI session filtering and sorting
-- CREATE INDEX idx_ai_sessions_user_status_created ON ai_generation_sessions(user_id, status, created_at DESC);
```

## 4. PostgreSQL Row Level Security (RLS) Policies

### Enable RLS on tables
```sql
ALTER TABLE flashcards ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_generation_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
```

### RLS Policies
```sql
-- Flashcards: Users can only access their own flashcards
CREATE POLICY flashcards_user_policy ON flashcards
    USING (user_id = current_setting('app.current_user_id')::UUID);

-- AI Generation Sessions: Users can only access their own sessions
CREATE POLICY ai_sessions_user_policy ON ai_generation_sessions
    USING (user_id = current_setting('app.current_user_id')::UUID);

-- User Roles: Users can only access their own roles
CREATE POLICY user_roles_user_policy ON user_roles
    USING (user_id = current_setting('app.current_user_id')::UUID);
```

### Application Configuration
The Spring Boot application must set the current user context:
```sql
-- Set current user ID in PostgreSQL session
SET app.current_user_id = '<current_user_uuid>';
```

## 5. Additional Design Notes and Explanations

### UUID Generation Strategy
- All primary keys use UUID type
- UUIDs are generated in Java application code, not in database
- Provides better distribution and prevents enumeration attacks
- Suitable for future horizontal scaling

### GDPR Compliance
- Hard delete approach with CASCADE DELETE ensures complete data removal
- No soft delete columns to comply with "right to be forgotten"
- Account deletion removes all associated data (flashcards, AI sessions, roles)

### Source Tracking for Flashcards
- `source` column tracks flashcard origin: 'AI', 'AI_USER', 'USER'
- 'AI': Generated by AI and accepted without modification
- 'AI_USER': Generated by AI but modified by user before acceptance
- 'USER': Manually created by user
- Enables analytics on AI generation effectiveness

### AI Session Tracking
- Comprehensive tracking of AI generation requests
- Cost monitoring for API usage optimization
- Status tracking for error handling and user feedback
- Model tracking for quality analysis across different AI models

### Timestamp Management
- All tables include `created_at` for audit trails
- User-modifiable entities include `updated_at`
- TIMESTAMPTZ ensures timezone consistency
- Default to CURRENT_TIMESTAMP for automatic population

### Security Considerations
- Row Level Security prevents cross-user data access
- CHECK constraints ensure data integrity
- Foreign key constraints maintain referential integrity
- Password column designed for hashed values (BCrypt recommended)

### Scalability Design
- Strategic indexing for common query patterns
- Connection pooling configuration (HikariCP max 10 connections for MVP)
- UUID primary keys support future sharding
- Normalized structure (3NF) with appropriate denormalization for performance