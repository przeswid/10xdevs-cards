-- COMPLETE ROLLBACK SCRIPT FOR RELEASE 1.0 REFACTOR
-- ===================================================
-- 
-- This script provides manual rollback for the complete 1.0 refactor
-- Use this if Liquibase automatic rollback fails or for emergency rollback
-- 
-- EXECUTION ORDER: Reverse of creation order due to dependencies
-- 
-- WARNING: This will permanently delete ALL data in the database
-- This is acceptable as we're not in production yet
-- Make sure to backup data before executing if needed

-- =============================================
-- STEP 1: DROP RLS POLICIES AND DISABLE RLS
-- =============================================

-- Drop RLS policies
DROP POLICY IF EXISTS flashcards_user_policy ON flashcards;
DROP POLICY IF EXISTS ai_sessions_user_policy ON ai_generation_sessions;
DROP POLICY IF EXISTS user_roles_user_policy ON user_roles;

-- Disable RLS
ALTER TABLE flashcards DISABLE ROW LEVEL SECURITY;
ALTER TABLE ai_generation_sessions DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles DISABLE ROW LEVEL SECURITY;

-- =============================================
-- STEP 2: DROP FOREIGN KEY CONSTRAINTS
-- =============================================

-- Drop foreign keys in reverse dependency order
ALTER TABLE flashcards DROP CONSTRAINT IF EXISTS fk_flashcards_generation_session_id;
ALTER TABLE flashcards DROP CONSTRAINT IF EXISTS fk_flashcards_user_id;
ALTER TABLE ai_generation_sessions DROP CONSTRAINT IF EXISTS fk_ai_sessions_user_id;
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_user_id;

-- =============================================
-- STEP 3: DROP INDEXES
-- =============================================

-- Drop flashcard indexes
DROP INDEX IF EXISTS idx_flashcards_updated_at;
DROP INDEX IF EXISTS idx_flashcards_created_at;
DROP INDEX IF EXISTS idx_flashcards_generation_session_id;
DROP INDEX IF EXISTS idx_flashcards_user_source;
DROP INDEX IF EXISTS idx_flashcards_source;
DROP INDEX IF EXISTS idx_flashcards_user_updated;
DROP INDEX IF EXISTS idx_flashcards_user_created;
DROP INDEX IF EXISTS idx_flashcards_user_id;

-- Drop AI generation session indexes
DROP INDEX IF EXISTS idx_ai_sessions_model_cost_time;
DROP INDEX IF EXISTS idx_ai_sessions_ai_model;
DROP INDEX IF EXISTS idx_ai_sessions_created_at;
DROP INDEX IF EXISTS idx_ai_sessions_status;
DROP INDEX IF EXISTS idx_ai_sessions_user_created;
DROP INDEX IF EXISTS idx_ai_sessions_user_id;

-- Drop user_roles indexes
DROP INDEX IF EXISTS idx_user_roles_role;
DROP INDEX IF EXISTS idx_user_roles_user_id;

-- Drop users indexes
DROP INDEX IF EXISTS idx_users_last_login_at;
DROP INDEX IF EXISTS idx_users_created_at;
DROP INDEX IF EXISTS idx_users_account_status;
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_username;

-- =============================================
-- STEP 4: DROP TABLES (REVERSE DEPENDENCY ORDER)
-- =============================================

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS flashcards;
DROP TABLE IF EXISTS ai_generation_sessions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;

-- =============================================
-- STEP 5: CLEAN UP LIQUIBASE TRACKING
-- =============================================

-- Remove all changesets from this refactor (optional)
-- Uncomment if you want to completely reset and re-run the migration
/*
DELETE FROM databasechangelog 
WHERE id IN (
    'create-table-users',
    'create-table-user-roles', 
    'create-table-ai-generation-sessions',
    'create-table-flashcards',
    'create-indexes-users-authentication',
    'create-indexes-users-administration',
    'create-indexes-user-roles',
    'create-indexes-ai-sessions-user-queries',
    'create-indexes-ai-sessions-status-monitoring',
    'create-indexes-ai-sessions-analytics',
    'create-indexes-flashcards-user-queries',
    'create-indexes-flashcards-analytics',
    'create-indexes-flashcards-temporal',
    'create-fk-user-roles-to-users',
    'create-fk-ai-sessions-to-users',
    'create-fk-flashcards-to-users',
    'create-fk-flashcards-to-ai-sessions',
    'enable-rls-user-roles',
    'create-rls-policy-user-roles',
    'enable-rls-ai-generation-sessions',
    'create-rls-policy-ai-sessions',
    'enable-rls-flashcards',
    'create-rls-policy-flashcards'
);
*/

-- =============================================
-- STEP 6: VERIFICATION QUERIES
-- =============================================

-- Verify all tables are dropped
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('users', 'user_roles', 'ai_generation_sessions', 'flashcards');

-- Verify no remaining indexes (should return empty)
SELECT indexname 
FROM pg_indexes 
WHERE tablename IN ('users', 'user_roles', 'ai_generation_sessions', 'flashcards');

-- Verify no remaining constraints (should return empty)
SELECT constraint_name, table_name 
FROM information_schema.table_constraints 
WHERE table_schema = 'public' 
AND table_name IN ('users', 'user_roles', 'ai_generation_sessions', 'flashcards');

-- Check remaining Liquibase entries
SELECT id, author, filename, dateexecuted 
FROM databasechangelog 
WHERE author = 'migration-team'
ORDER BY dateexecuted DESC;

-- =============================================
-- POST-ROLLBACK ACTIONS
-- =============================================

-- If you want to restore the original 001_init_schema.sql structure:
-- 1. Update application.yml to point back to original changelog
-- 2. Run the application to execute 001_init_schema.sql
-- 3. Or manually execute the original SQL file

-- Original minimal schema (if needed):
/*
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100)
);

CREATE TABLE user_roles (
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
*/

COMMIT;