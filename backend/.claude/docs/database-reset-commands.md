# Database Reset Commands for Refactoring

## Complete Database Reset (Run these commands in PostgreSQL)

Since you're not in production and data loss is acceptable, run these commands to ensure a clean migration:

```sql
-- Connect to PostgreSQL and run these commands:

-- Drop all existing tables (if any)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop Liquibase tracking table to start fresh
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;

-- Verify clean state
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- Should return empty result

-- Optional: Verify no remaining sequences
SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public';
```

## OR use this one-liner:
```bash
# Complete database recreation (if needed)
dropdb tenx_cards && createdb tenx_cards
```

After cleaning, your next application startup will execute the new XML-based changelog and create the complete MVP schema.