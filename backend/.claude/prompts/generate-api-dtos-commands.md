# Java DTO and Command Model Generation Task

You are a qualified Java programmer whose task is to create a library of DTO (Data Transfer Object) and Command (CQRS) types for an application. Your task is to analyze the database model definition and API plan, then create appropriate DTO types that accurately represent the data structures required by the API, while maintaining the connection with the underlying database models.

## Input Data

First, carefully review the following inputs:

### 1. Database Models
```
Stored in the liquibase: @src/main/resources/db/changelog/liquibase/
```

### 2. API Plan
```
@api-plan.md (containing defined DTOs)
```

## Task Steps

Your task is to create Java type definitions for DTOs and Commands (Pipelinr) specified in the API plan, ensuring they derive from the database models. Follow these steps:

1. Analyze the database models and API plan.
2. Create DTO types and Command Models based on the API plan, using database entity definitions.
3. Ensure compatibility between DTOs and Command Models with API requirements.
4. Apply appropriate Java language features to create, narrow, or extend types as needed.
5. Perform a final check to ensure all DTOs are included and properly connected to entity definitions.

## Analysis Process

Before creating the final output, work inside `<dto_analysis>` tags in your thinking block to show your thought process and ensure all requirements are met.

In your analysis:
- List all DTOs and Command Models defined in the API plan, numbering each one.
- For each DTO and Command Model:
    - Identify the corresponding database entities and any necessary type transformations.
    - Describe the Java features or tools you plan to use.
    - Create a brief sketch of the DTO and Command Model structure.
- Explain how you will ensure that each DTO and Command Model is directly or indirectly connected to entity type definitions.

## Package Structure

After conducting the analysis, provide the final DTO and Command type definitions that will appear in the following packages:

### Users Bounded Context
*(includes users and user_roles tables)*

#### Request DTOs
- **Package**: `com.ten.devs.cards.cards.user.presentation.request`
- **Purpose**: All DTOs related to endpoint input (request)
- **Format**: `[Action Name]Request.java`
- **Note**: Files already exist in this package - adapt them to new requirements if necessary. Check existing ones.

#### Response DTOs
- **Package**: `com.ten.devs.cards.cards.user.presentation.response`
- **Purpose**: All DTOs related to data returned by endpoints
- **Format**: `[Response Name]Response.java`
- **Note**: Files already exist in this package - adapt them to new requirements if necessary. Check existing ones.

#### Commands
- **Package**: `com.ten.devs.cards.cards.user.application.command`
- **Purpose**: All commands as records implementing `Command<>` from Pipelinr
- **Example**:
```java
public record LoginUserCommand(String username, String password) implements Command<LoginResponse> {
}
```
- **Note**: Files already exist in this package - adapt them to new requirements if necessary.

### Flashcards Bounded Context
*(includes flashcards and ai_generation_sessions tables)*

#### Request DTOs
- **Package**: `com.ten.devs.cards.cards.flashcards.presentation.request`
- **Purpose**: All DTOs related to endpoint input (request)

#### Response DTOs
- **Package**: `com.ten.devs.cards.cards.flashcards.presentation.response`
- **Purpose**: All DTOs related to data returned by endpoints

#### Commands
- **Package**: `com.ten.devs.cards.cards.flashcards.application.command`
- **Purpose**: All commands as records implementing `Command<>` from Pipelinr
- **Example**:
```java
public record LoginUserCommand(String username, String password) implements Command<LoginResponse> {
}
```

## Guidelines

- Use clear and descriptive names for your types
- Add comments to explain complex type manipulations or non-obvious relationships

## Important Reminders

- ✅ Ensure all DTOs and Commands defined in the API plan are included
- ✅ Each DTO and Command Model should directly reference one or more database entities
- ✅ Add comments to explain complex or non-obvious type manipulations