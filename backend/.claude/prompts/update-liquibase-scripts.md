
## Context
You are a Liquibase expert tasked with refactoring an existing Liquibase setup. The current project has:
- **Current State**: Single monolithic file `001_init_schema.sql` containing all database objects
- **Target State**: Modular structure with each table in a separate changelog file - refactoring of 001_init_schema.sql into multiple files. We can loose all the data in the DB as we're not in the PROD yet!
- **Goal**: Production-ready, maintainable Liquibase configuration following best practices

## Input
I will provide you with:
1. The contents of `001_init_schema.sql` containing CREATE TABLE, CREATE INDEX, and other DDL statements
2. Any existing foreign key relationships and dependencies

## Required Output Structure

### 1. File Organization
Generate the following directory structure:
```
liquibase/
├── changelog-master.xml
├── changelogs/
│   ├── releases/
│   │   └── 1.0/
│   │       ├── changelog-1.0-refactored.xml
│   │       └── tables/
│   │           ├── 001-create-table-[table1].xml
│   │           ├── 002-create-table-[table2].xml
│   │           └── ...
│   │       └── indexes/
│   │           ├── 001-create-indexes-[table1].xml
│   │           └── ...
│   │       └── constraints/
│   │           ├── 001-create-foreign-keys.xml
│   │           └── ...
│   │       └── data/
│   │           └── 001-seed-data.xml
└── rollback/
    └── rollback-1.0.sql
```

## Refactoring Requirements

### Phase 1: Refactoring of current solution
 `001_init_schema.sql` which contains init data should be replaced with xml-based changelogs.
We're not on PROD yet, so loosing all data is acceptable.

### Phase 2: Individual Table Files
For each table, create a separate file.

## Best Practices to Implement

### 1. Changeset Rules
- **One change per changeset** - Never combine multiple DDL operations
- **ID Format**: `[operation]-[object]-[name]` (e.g., `create-table-users`, `add-index-users-email`)
- **Author**: Use consistent author names (e.g., team name or "migration")
- **Never modify existing changesets** - only add new ones

### 2. Dependency Management
Order files based on dependencies:
```
1. Core/reference tables (no foreign keys)
2. Dependent tables (ordered by dependency)
3. Junction/association tables
4. Indexes (separate files)
5. Foreign key constraints (after all tables)
6. Views/Stored procedures
7. Initial/seed data
```

### 3. Naming Conventions
- **Tables**: `snake_case`
- **Constraints**:
    - Primary Keys: `pk_[table_name]`
    - Foreign Keys: `fk_[table]_[column]_[ref_table]`
    - Unique: `uq_[table]_[columns]`
    - Check: `ck_[table]_[description]`
- **Indexes**: `idx_[table]_[columns]`

### 4. Data Types (Database Agnostic)
```xml
<!-- Use Liquibase generic types -->
<column name="id" type="BIGINT" autoIncrement="true"/>
<column name="uuid" type="UUID"/>
<column name="name" type="VARCHAR(255)"/>
<column name="description" type="TEXT"/>
<column name="amount" type="DECIMAL(19,4)"/>
<column name="is_active" type="BOOLEAN" defaultValueBoolean="true"/>
<column name="created_at" type="TIMESTAMP" defaultValueComputed="${now}"/>
```

### 5. Documentation Requirements
Each file must include:
- Business purpose comments
- Table/column remarks
- Rollback statements
- TODO comments for business decisions

## Special Instructions

### Output Requirements
0. **Refactor `001_init_schema.sql`** into modular Liquibase XML files
1. **Generate separate files** for each table
2. **Create a dependency map** showing table relationships
3. **Provide rollback strategy** for the refactoring
4**Generate validation queries** to ensure schema integrity
### Additional Context Needed
Please also:
- Identify any circular dependencies and suggest resolution
- Flag any anti-patterns in the original schema
- Suggest performance optimizations (indexes, partitioning)
- Identify missing constraints or potential data integrity issues
- Recommend contexts and labels based on the schema purpose
- 
---

**Generate the refactored Liquibase scripts following all guidelines above, ensuring backward compatibility with the existing `001_init_schema.sql` deployment.**