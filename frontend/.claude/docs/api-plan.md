# REST API Plan

## 1. Resources

- **Users** → `users` table - User accounts with authentication credentials and profile information
- **User Roles** → `user_roles` table - User authorization roles and permissions
- **AI Generation Sessions** → `ai_generation_sessions` table - AI flashcard generation tracking and monitoring
- **Flashcards** → `flashcards` table - Flashcard content (both manual and AI-generated)

## 2. Endpoints

### 2.1 Authentication and Authorization

#### POST /auth/register
**Description:** Register new user account
**Request Body:**
```json
{
  "username": "string (1-50 chars, unique, required)",
  "email": "string (valid email, max 254 chars, unique, required)",
  "password": "string (min 8 chars, required)",
  "firstName": "string (optional, max 100 chars)",
  "lastName": "string (optional, max 100 chars)"
}
```
**Success Response:** `200 OK`
```json
"uuid"
```
**Error Responses:**
- `400 Bad Request` - Invalid input data or validation errors
- `409 Conflict` - Username or email already exists

#### POST /auth/login
**Description:** Authenticate user and return JWT token
**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```
**Success Response:** `200 OK`
```json
{
  "username": "string",
  "accessToken": "jwt_token",
  "expiresIn": 3600
}
```
**Error Responses:**
- `400 Bad Request` - Missing username or password
- `401 Unauthorized` - Invalid credentials

### 2.2 AI Flashcard Generation

#### POST /ai/sessions
**Description:** Start AI flashcard generation session  
**Headers:** `Authorization: Bearer {token}`
**Request Body:**
```json
{
  "inputText": "string (1000-10000 chars, required)"
}
```
**Success Response:** `201 Created`
```json
{
  "sessionId": "uuid",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00Z"
}
```
**Error Responses:**
- `400 Bad Request` - Invalid input text length
- `401 Unauthorized` - Invalid or missing token
- `422 Unprocessable Entity` - AI service unavailable

#### GET /ai/sessions/{sessionId}
**Description:** Get AI generation session status and metrics
**Headers:** `Authorization: Bearer {token}`
**Success Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "status": "PENDING|COMPLETED|FAILED",
  "generatedCount": 12,
  "acceptedCount": 8,
  "aiModel": "gpt-4",
  "apiCost": 0.0250,
  "createdAt": "2024-01-15T10:30:00Z"
}
```
**Error Responses:**
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Session belongs to different user
- `404 Not Found` - Session not found

#### GET /ai/sessions/{sessionId}/suggestions
**Description:** Retrieve AI-generated flashcard suggestions  
**Headers:** `Authorization: Bearer {token}`
**Success Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "status": "COMPLETED",
  "suggestions": [
    {
      "suggestionId": "uuid",
      "frontContent": "What is the capital of France?",
      "backContent": "Paris"
    }
  ]
}
```
**Error Responses:**
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Session belongs to different user
- `404 Not Found` - Session not found
- `409 Conflict` - Session not completed yet

#### POST /ai/sessions/{sessionId}/approve
**Description:** Approve and save selected AI-generated flashcard suggestions  
**Headers:** `Authorization: Bearer {token}`
**Request Body:**
```json
{
  "approvedSuggestions": [
    {
      "suggestionId": "uuid",
      "frontContent": "string (optional - if edited, max 1000 chars)",
      "backContent": "string (optional - if edited, max 1000 chars)"
    }
  ]
}
```
**Success Response:** `201 Created`
```json
{
  "createdFlashcards": [
    {
      "flashcardId": "uuid",
      "frontContent": "What is the capital of France?",
      "backContent": "Paris",
      "source": "AI"
    }
  ]
}
```
**Error Responses:**
- `400 Bad Request` - Invalid content length or suggestion IDs
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Session belongs to different user
- `404 Not Found` - Session or suggestions not found

### 2.3 Flashcard Management

#### GET /flashcards
**Description:** List user's flashcards with pagination and filtering  
**Headers:** `Authorization: Bearer {token}`
**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20, max: 100)  
- `sort` (optional, default: "createdAt,desc")
- `source` (optional, filter: AI|AI_USER|USER)

**Success Response:** `200 OK`
```json
{
  "content": [
    {
      "flashcardId": "uuid",
      "frontContent": "What is the capital of France?",
      "backContent": "Paris",
      "source": "AI",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```
**Error Responses:**
- `400 Bad Request` - Invalid pagination or sort parameters
- `401 Unauthorized` - Invalid or missing token

#### POST /flashcards
**Description:** Create manual flashcard  
**Headers:** `Authorization: Bearer {token}`
**Request Body:**
```json
{
  "frontContent": "string (1-1000 chars, required)",
  "backContent": "string (1-1000 chars, required)"
}
```
**Success Response:** `201 Created`
```json
{
  "flashcardId": "uuid",
  "frontContent": "What is the capital of Spain?",
  "backContent": "Madrid",
  "source": "USER",
  "createdAt": "2024-01-15T10:30:00Z"
}
```
**Error Responses:**
- `400 Bad Request` - Invalid content length or missing fields
- `401 Unauthorized` - Invalid or missing token

#### PUT /flashcards/{flashcardId}
**Description:** Edit existing flashcard  
**Headers:** `Authorization: Bearer {token}`
**Request Body:**
```json
{
  "frontContent": "string (1-1000 chars, required)",
  "backContent": "string (1-1000 chars, required)"
}
```
**Success Response:** `200 OK`
```json
{
  "flashcardId": "uuid",
  "frontContent": "What is the capital of Spain?",
  "backContent": "Madrid",
  "source": "AI_USER",
  "updatedAt": "2024-01-15T11:45:00Z"
}
```
**Error Responses:**
- `400 Bad Request` - Invalid content length or missing fields
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Flashcard belongs to different user
- `404 Not Found` - Flashcard not found

#### DELETE /flashcards/{flashcardId}
**Description:** Delete flashcard permanently  
**Headers:** `Authorization: Bearer {token}`
**Success Response:** `204 No Content`
**Error Responses:**
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Flashcard belongs to different user
- `404 Not Found` - Flashcard not found

## 3. Authentication and Authorization

### 3.1 Authentication Mechanism
- **JWT Bearer Token** authentication for all protected endpoints
- Tokens include user ID, username, and roles in claims
- Configurable token expiration (default: 1 hour)
- Public endpoints: `/auth/register`, `/auth/login`

### 3.2 Authorization Strategy
- **Row Level Security (RLS)** implemented at database level
- Users can only access their own data (flashcards, AI sessions)
- Spring Security context sets `app.current_user_id` for RLS policies
- Role-based access control for future admin features

### 3.3 Security Headers
- All API responses include security headers:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-XSS-Protection: 1; mode=block`

## 4. Validation and Business Logic

### 4.1 Validation Rules

**User Registration:**
- Username: 1-50 characters, unique, alphanumeric + underscore
- Email: Valid email format, max 254 characters, unique
- Password: Minimum 8 characters, must contain uppercase, lowercase, digit

**AI Generation:**
- Input text: 1000-10000 characters (enforced by database constraint)
- Session status: Must be one of PENDING, COMPLETED, FAILED

**Flashcard Content:**
- Front content: 1-1000 characters, required
- Back content: 1-1000 characters, required
- Source tracking: AI (unmodified), AI_USER (edited), USER (manual)

### 4.2 Business Logic Implementation

**AI Generation Workflow:**
1. Create session with PENDING status
2. Async call to OpenRouter API with input text
3. Parse AI response into structured flashcard suggestions
4. Update session status to COMPLETED/FAILED
5. Track metrics: generated_count, api_cost, ai_model

**Flashcard Source Tracking:**
- AI: Generated by AI and approved without modification
- AI_USER: Generated by AI but edited before approval
- USER: Manually created by user
- Enables analytics on AI generation effectiveness (target: 75% acceptance)

### 4.3 Error Handling Standards

**Standard Error Response Format:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Input text must be between 1000 and 10000 characters",
  "path": "/ai/sessions"
}
```

**Rate Limiting:**
- AI generation: 10 requests per hour per user
- Flashcard creation: 100 requests per hour per user
- Authentication: 5 failed attempts per 15 minutes per IP

### 4.4 Performance Considerations

**Pagination and Filtering:**
- Default page size: 20, maximum: 100
- Sorting by: createdAt, updatedAt (both ASC/DESC)
- Filtering by source type for flashcards
- Database indexes support efficient pagination

**Caching Strategy:**
- JWT token validation cached for 5 minutes
- User profile data cached for 15 minutes
- AI session status cached for 30 seconds during generation

**Monitoring and Analytics:**
- Track AI generation metrics for cost optimization
- Monitor API usage patterns for performance tuning
- Log security events for audit trails