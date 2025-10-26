# Database Planning Summary - 10x-cards MVP

## Conversation Summary

### Decisions Made

1. Added authentication metadata to `users` table (last_login_at, created_at, updated_at, account_status)
2. One-to-many relationship between users and flashcards (one flashcard has only one owner)
3. Store flashcard content as two text columns (front_content, back_content)
4. No separate table for AI sessions - added `source` column to flashcards with values: 'ai', 'ai_user', 'user'
5. Implement hard delete for GDPR compliance (cascade deletion)
6. Skip repetition algorithm implementation at this stage
7. Add indexes on frequently used columns
8. Implement RLS (Row Level Security) for user data isolation
9. Add audit columns (created_at, updated_at) to flashcards
10. Text constraints: TEXT with CHECK constraint (1000-10000 chars) for input_text, VARCHAR(1000) for flashcard content
11. Use CHECK constraint for `source` column with allowed values
12. Naming convention: plural for table names (users, flashcards)
13. Add AI metadata: ai_model and api_cost columns in ai_generation_sessions
14. Use UUID as primary keys (generated in Java, not database)
15. Limit email to VARCHAR(254) according to RFC 5321
16. Keep column name `password` (not password_hash)
17. Add AI session status (pending, completed, failed)
18. Key conventions: `id` for primary keys, `{table_name}_id` for foreign keys
19. No soft delete - stick with hard delete
20. No dedicated user_sessions table - rely on JWT tokens

### Matched Recommendations

1. **Table Architecture**: Structure of three main tables (users, flashcards, ai_generation_sessions) with clear relationships
2. **GDPR Security**: Hard delete with cascade deletion to ensure "right to be forgotten"
3. **Data Integrity**: CHECK constraints for source, status and account_status columns
4. **Performance**: Strategic indexes on user_id, email and other frequently used columns
5. **Row Level Security**: RLS implementation for data isolation between users
6. **Audit Trail**: Timestamp columns (created_at, updated_at) for change tracking
7. **Data Validation**: Text length constraints aligned with product requirements (1000-10000 characters)
8. **Connection Pooling**: HikariCP with maximum 10 connections for MVP
9. **Timezone Handling**: TIMESTAMPTZ with UTC in application for international consistency
10. **AI Monitoring**: Columns for tracking AI models and API costs

### Database Planning Summary

#### Main Database Schema Requirements

The database schema was designed for the 10x-cards MVP application, supporting automatic flashcard generation via AI and manual card management by users. The system requires:

- **User Authentication** with basic data and metadata (last login, account status)
- **Flashcard Management** with source origin distinction (AI, AI modified, manual)
- **AI Session Tracking** with cost and model information
- **GDPR Compliance** through hard delete implementation

#### Key Entities and Relationships

**Users**
- UUID as primary key, basic data (username, email, password)
- Metadata: last_login_at, account_status, created_at, updated_at
- 1:N relationship with flashcards and ai_generation_sessions

**Flashcards**
- UUID as primary key, front_content and back_content (VARCHAR 1000)
- Source column: 'ai', 'ai_user', 'user' for origin tracking
- Foreign key to users (CASCADE DELETE) and optionally to ai_generation_sessions
- Audit trail: created_at, updated_at

**AI_Generation_Sessions**
- UUID as primary key, input_text (TEXT with CHECK constraint 1000-10000 characters)
- Metrics: generated_count, accepted_count, ai_model, api_cost
- Status: pending, completed, failed
- Foreign key to users (CASCADE DELETE)

#### Security and Scalability

**Security:**
- Row Level Security (RLS) on all main tables
- RLS policies checking user_id = current_user_id()
- Hard delete with CASCADE for GDPR compliance
- CHECK constraints for data integrity

**Scalability:**
- Strategic indexes on user_id, email
- HikariCP connection pool (max 10 connections)
- TIMESTAMPTZ with UTC for international user support
- UUID primary keys generated in application (Java)

#### Technical Configuration

- **PostgreSQL** with default `public` schema
- **Liquibase** for migration management (planned split into separate files)
- **Spring Boot** integration with HikariCP
- **Conventions**: plural for tables, `id` for PK, `{table}_id` for FK

### Unresolved Issues

1. **Liquibase Structure**: Implementation details for splitting 001_init_schema.sql migration into separate files per table
2. **RLS Implementation**: Concrete implementation for passing user_id from Spring Security context to PostgreSQL
3. **AI Model Configuration**: Details of Openrouter.ai integration configuration in context of metadata storage
4. **Indexing Strategy**: Possibility of adding composite indexes in the future (e.g., flashcards(user_id, created_at DESC))
5. **Backup Strategy**: GDPR-compliant backup plan creation