# Authentication System Technical Specification

## Document Overview

This specification defines the architecture for implementing user authentication (registration and login) in the 10x-cards frontend application. The design ensures compatibility with the existing Spring Boot backend API and maintains the current application behavior for flashcard generation and management.

---

## 1. USER INTERFACE ARCHITECTURE

### 1.1 Page Structure & Routing

#### 1.1.1 New Public Pages (Unauthenticated)

**Registration Page** (`/register`)
- **Type**: Astro page component
- **Purpose**: User account creation
- **Access**: Public, redirects to `/generate` if already authenticated
- **Layout**: Uses base `Layout.astro`
- **Components**:
  - `RegisterForm` (React component, client-side rendered)
  - Form validation indicators
  - Link to login page

**Login Page** (`/login`)
- **Type**: Astro page component
- **Purpose**: User authentication
- **Access**: Public, redirects to `/generate` if already authenticated
- **Layout**: Uses base `Layout.astro`
- **Components**:
  - `LoginForm` (React component, client-side rendered)
  - Form validation indicators
  - Link to registration page

**Landing Page** (`/`) - Modified
- **Current State**: Displays Welcome component
- **New Behavior**:
  - For unauthenticated users: Shows welcome message with call-to-action buttons for register/login
  - For authenticated users: Redirects to `/generate`
- **Components**:
  - `Welcome.astro` - Extended with authentication-aware content
  - Navigation buttons to `/register` and `/login`

#### 1.1.2 Protected Pages (Require Authentication)

**Generate Flashcards Page** (`/generate`) - Protected
- **Current State**: Publicly accessible, contains `GenerateApp` React component
- **New Behavior**: Requires authentication, redirects to `/login` if not authenticated
- **Authentication Check**: Server-side check in Astro page via cookie/token validation
- **No Changes**: Existing React components remain unchanged

**Flashcards Management Page** (`/flashcards`) - Protected
- **Current State**: Stub page, publicly accessible
- **New Behavior**: Requires authentication, redirects to `/login` if not authenticated
- **Future Implementation**: Will display user's flashcard collection

#### 1.1.3 Navigation Component

**AppHeader/Navigation Component** (New)
- **Type**: Astro component with optional React interactivity
- **Purpose**: Consistent navigation across all pages
- **Authentication-Aware Behavior**:
  - **Unauthenticated State**: Show "Login" and "Register" buttons
  - **Authenticated State**: Show "Generate", "Flashcards", username display, "Logout" button
- **Placement**: Integrated into `Layout.astro` or as slot content
- **Styling**: Uses Shadcn/ui components, Tailwind CSS

### 1.2 Component Architecture

#### 1.2.1 Authentication Forms (React Components)

**RegisterForm Component** (`src/components/auth/RegisterForm.tsx`)
- **Responsibility**: User registration form with client-side validation
- **Client-Side**: React component with state management for form inputs
- **Integration**: Uses authentication service for API calls
- **Fields**:
  - username (string, 3-50 chars)
  - password (string, 8-100 chars, password input)
  - email (string, valid email format, max 100 chars)
  - firstName (string, 1-50 chars)
  - lastName (string, 1-50 chars)
- **Validation**: Real-time validation using validation service
- **User Feedback**:
  - Field-level error messages
  - Form-level error for API failures
  - Success message and auto-redirect on successful registration
- **UI Components**: Uses Shadcn/ui Input, Button, Label, Alert

**LoginForm Component** (`src/components/auth/LoginForm.tsx`)
- **Responsibility**: User authentication form with client-side validation
- **Client-Side**: React component with state management
- **Integration**: Uses authentication service for API calls
- **Fields**:
  - username (string, required)
  - password (string, required)
- **Validation**: Required field validation
- **User Feedback**:
  - Field-level error messages
  - "Invalid credentials" message for 401 responses
  - Network error messages for connection issues
  - Success indication and redirect on successful login
- **UI Components**: Uses Shadcn/ui Input, Button, Label, Alert

#### 1.2.2 Authentication Context (React)

**AuthContext/AuthProvider** (`src/lib/context/AuthContext.tsx`)
- **Purpose**: Global authentication state management for React components
- **State Management**:
  - Current user information (username, email, firstName, lastName, id)
  - Authentication status (loading, authenticated, unauthenticated)
  - JWT token (stored in memory, not directly exposed)
- **Methods Exposed**:
  - `login(username, password)` - Authenticates user and stores token
  - `register(userData)` - Registers new user and auto-login
  - `logout()` - Clears token and user data
  - `isAuthenticated()` - Boolean check
  - `getAuthToken()` - Returns current JWT for API calls
- **Token Management**:
  - Stores JWT in localStorage for persistence
  - Loads token on app initialization
  - Provides token to axios interceptor
- **Synchronization**: Works with Astro's server-side auth checks via cookie/localStorage

#### 1.2.3 Layout Extensions

**Layout.astro Modifications**
- **Current State**: Basic HTML wrapper with global styles
- **Extensions Needed**:
  - Option to include navigation header (via props or slot)
  - Pass authentication state from server-side to client-side React components
  - Support for protected page indicators
- **Props**:
  - `title` (existing)
  - `requireAuth?: boolean` (new) - Triggers redirect if not authenticated
  - `showNavigation?: boolean` (new, default: true)
- **Server-Side Logic**:
  - Check for authentication token in cookies
  - Redirect to `/login` if `requireAuth=true` and not authenticated
  - Pass user data to client-side if authenticated

### 1.3 Validation & Error Handling

#### 1.3.1 Client-Side Validation Rules

**Registration Form Validation**:
- **username**:
  - Required
  - Minimum 3 characters
  - Maximum 50 characters
  - Pattern: Alphanumeric and underscore only
  - Error: "Username must be 3-50 characters (letters, numbers, underscore)"

- **password**:
  - Required
  - Minimum 8 characters
  - Maximum 100 characters
  - Must contain: 1 uppercase, 1 lowercase, 1 number, 1 special character
  - Error: "Password must be 8-100 chars with uppercase, lowercase, number, and special character"

- **email**:
  - Required
  - Valid email format (RFC 5322 subset)
  - Maximum 100 characters
  - Error: "Please enter a valid email address"

- **firstName**:
  - Required
  - Minimum 1 character
  - Maximum 50 characters
  - Error: "First name is required (max 50 characters)"

- **lastName**:
  - Required
  - Minimum 1 character
  - Maximum 50 characters
  - Error: "Last name is required (max 50 characters)"

**Login Form Validation**:
- **username**: Required - "Username is required"
- **password**: Required - "Password is required"

#### 1.3.2 API Error Handling

**Registration Errors** (`POST /auth/register`):
- **400 Bad Request**:
  - Display specific field errors from backend response
  - Fallback: "Invalid registration data. Please check your inputs."
- **409 Conflict** (username/email exists):
  - "This username or email is already registered. Please try another."
- **Network Error**:
  - "Unable to connect to server. Please check your connection."
- **Unknown Error**:
  - "Registration failed. Please try again later."

**Login Errors** (`POST /auth/login`):
- **401 Unauthorized**:
  - "Invalid username or password. Please try again."
- **400 Bad Request**:
  - "Invalid login data. Please check your inputs."
- **Network Error**:
  - "Unable to connect to server. Please check your connection."
- **Unknown Error**:
  - "Login failed. Please try again later."

**Protected Resource Errors** (API calls with JWT):
- **401 Unauthorized** (Invalid/expired token):
  - Clear local authentication state
  - Redirect to `/login`
  - Show message: "Your session has expired. Please log in again."
- **403 Forbidden** (Valid token, insufficient permissions):
  - Show message: "You don't have permission to access this resource."
  - Stay on current page

#### 1.3.3 Validation Service

**Validation Module** (`src/lib/validation/auth.ts`)
- **Purpose**: Centralized validation logic reusable across components
- **Functions**:
  - `validateUsername(username: string): ValidationResult`
  - `validatePassword(password: string): ValidationResult`
  - `validateEmail(email: string): ValidationResult`
  - `validateName(name: string, fieldName: string): ValidationResult`
  - `validateRegistrationForm(formData): FormValidationResult`
  - `validateLoginForm(formData): FormValidationResult`
- **Return Type**: `{ isValid: boolean, errors: string[] }`
- **Usage**: Used by form components before submission

### 1.4 User Experience Flows

#### 1.4.1 Registration Flow

1. **User navigates to `/register`**
   - Page loads with empty form
   - All fields show neutral state

2. **User fills out form**
   - Real-time validation on blur/change
   - Invalid fields show red border and error message below field
   - Valid fields show no error (or green checkmark for enhanced UX)

3. **User submits form**
   - Button shows loading state ("Registering...")
   - Form fields disabled during submission
   - Client-side validation runs first
   - If invalid: Show errors, stop submission

4. **API Request Success**
   - Show success message: "Registration successful! Redirecting..."
   - Store JWT token in localStorage
   - Set authentication state in AuthContext
   - Redirect to `/generate` after 1 second

5. **API Request Failure**
   - Show error alert at top of form
   - Re-enable form fields
   - Keep user input intact
   - Focus on first error field (if field-specific)

#### 1.4.2 Login Flow

1. **User navigates to `/login`**
   - Page loads with empty form
   - Show link: "Don't have an account? Register"

2. **User fills out credentials**
   - Basic required field validation

3. **User submits form**
   - Button shows loading state ("Signing in...")
   - Form fields disabled during submission

4. **API Request Success**
   - Store JWT token (localStorage + AuthContext)
   - Extract expiration time from response
   - Redirect to `/generate` (or return URL if came from redirect)
   - No success message (immediate redirect)

5. **API Request Failure**
   - Show error alert: Context-specific message
   - Re-enable form
   - Clear password field for security
   - Focus username field

#### 1.4.3 Logout Flow

1. **User clicks logout button in navigation**
   - Immediate action, no confirmation dialog

2. **Client-side actions**
   - Clear JWT from localStorage
   - Clear AuthContext state
   - Clear any cached user data

3. **Redirect**
   - Navigate to `/login`
   - Show optional message: "You've been logged out successfully"

#### 1.4.4 Protected Page Access Flow

**Scenario A: Unauthenticated user tries to access `/generate`**
1. Astro server-side checks for auth token in cookies/localStorage signal
2. No valid token found
3. Server responds with redirect to `/login?redirect=/generate`
4. Login page stores `redirect` parameter
5. After successful login, redirects to `/generate`

**Scenario B: Authenticated user accesses `/generate`**
1. Server-side finds valid token in cookie
2. Page renders normally
3. React components use AuthContext for user data
4. API calls include JWT token via axios interceptor

**Scenario C: Token expires during session**
1. User performs action requiring API call
2. API returns 401 Unauthorized
3. Axios interceptor catches error
4. Clears authentication state
5. Redirects to `/login` with current page as return URL
6. Shows message about expired session

---

## 2. AUTHENTICATION SYSTEM

### 2.1 Backend Integration

#### 2.1.1 API Endpoints (Already Implemented in Backend)

**POST /auth/register**
- **Purpose**: Create new user account
- **Security**: Public endpoint (no JWT required)
- **Request Body**:
  ```typescript
  {
    username: string;      // 3-50 chars
    password: string;      // 8-100 chars
    email: string;         // valid email, max 100 chars
    firstName: string;     // 1-50 chars
    lastName: string;      // 1-50 chars
  }
  ```
- **Success Response (200)**:
  ```typescript
  string // UUID of created user
  ```
- **Error Responses**:
  - 400: Invalid request data (validation errors)
  - 409: Username or email already exists (inferred from spec)

**POST /auth/login**
- **Purpose**: Authenticate user and receive JWT token
- **Security**: Public endpoint (no JWT required)
- **Request Body**:
  ```typescript
  {
    username: string;
    password: string;
  }
  ```
- **Success Response (200)**:
  ```typescript
  {
    username: string;
    accessToken: string;    // JWT token
    expiresIn: number;      // seconds until expiration
  }
  ```
- **Error Responses**:
  - 401: Invalid credentials
  - 400: Invalid request format

#### 2.1.2 Protected Endpoints Pattern

**All endpoints except `/auth/register` and `/auth/login` require JWT authentication**

**Authorization Header Format**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Examples of Protected Endpoints**:
- `POST /ai/sessions` - Create AI generation session
- `GET /ai/sessions/{sessionId}` - Get session status
- `GET /ai/sessions/{sessionId}/suggestions` - Get suggestions
- `POST /ai/sessions/{sessionId}/approve` - Approve suggestions
- `GET /flashcards` - List user flashcards
- `POST /flashcards` - Create flashcard
- `PUT /flashcards/{flashcardId}` - Update flashcard
- `DELETE /flashcards/{flashcardId}` - Delete flashcard
- `GET /users/` - List users

**Protected Endpoint Error Responses**:
- 401 Unauthorized: No token, invalid token, or expired token
- 403 Forbidden: Valid token but insufficient permissions

### 2.2 Frontend Authentication Services

#### 2.2.1 Authentication API Service

**Service Module** (`src/lib/api/auth.ts`)
- **Purpose**: Encapsulate all authentication-related API calls
- **Dependencies**: Uses `apiClient` from `src/lib/api/client.ts`

**Service Functions**:

```typescript
// Register new user
register(userData: RegisterRequest): Promise<RegisterResponse>
  - Calls: POST /auth/register
  - Returns: { userId: string }
  - Throws: APIError with validation/conflict details

// Authenticate user
login(credentials: LoginRequest): Promise<LoginResponse>
  - Calls: POST /auth/login
  - Returns: { username, accessToken, expiresIn }
  - Throws: APIError for invalid credentials

// Logout (client-side only, no backend call)
logout(): void
  - Clears localStorage token
  - Clears AuthContext state
```

**Type Definitions** (`src/lib/api/types.ts` - extend existing file):
```typescript
interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
}

interface RegisterResponse {
  userId: string;
}

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  username: string;
  accessToken: string;
  expiresIn: number;
}

interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}
```

#### 2.2.2 Token Management Service

**Service Module** (`src/lib/services/tokenService.ts`)
- **Purpose**: Centralized JWT token storage and retrieval
- **Storage Strategy**: localStorage for persistence across browser sessions

**Service Functions**:

```typescript
// Store JWT token and expiration
setToken(token: string, expiresIn: number): void
  - Stores token in localStorage key: 'auth_token'
  - Calculates expiration timestamp
  - Stores expiration in localStorage key: 'auth_token_expiry'

// Retrieve current token if valid
getToken(): string | null
  - Reads from localStorage
  - Checks if token exists
  - Validates expiration timestamp
  - Returns token or null if expired/missing

// Check if token is valid and not expired
isTokenValid(): boolean
  - Returns boolean
  - Used for route guards and UI state

// Clear token (on logout or expiration)
clearToken(): void
  - Removes 'auth_token' from localStorage
  - Removes 'auth_token_expiry' from localStorage

// Get time until token expires (for auto-logout)
getTimeUntilExpiration(): number | null
  - Returns milliseconds until expiration
  - Returns null if no valid token
```

**Security Considerations**:
- localStorage is vulnerable to XSS but acceptable for MVP
- Token is httpOnly=false (frontend needs to read it)
- Future enhancement: Consider httpOnly cookies for added security
- Token transmission: Always over HTTPS in production

#### 2.2.3 Axios Interceptor Integration

**API Client Enhancement** (`src/lib/api/client.ts` - modify existing)

**Request Interceptor** (already stubbed in current code):
- **Current State**: TODO comment for adding auth token
- **New Implementation**:
  - Import `tokenService.getToken()`
  - Before each request, retrieve current token
  - If token exists and valid, add to Authorization header: `Bearer {token}`
  - If token expired, clear token and reject request (triggers logout flow)

**Response Interceptor** (existing error handler):
- **Current State**: Basic error logging
- **New Implementation**:
  - Detect 401 Unauthorized responses
  - Call `tokenService.clearToken()`
  - Emit event or use AuthContext to trigger logout
  - Redirect to `/login` with return URL
  - Optionally show "Session expired" message

**Interceptor Logic**:
```typescript
// Request Interceptor
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenService.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor (extend existing)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenService.clearToken();
      // Trigger logout flow (via event or direct navigation)
      window.location.href = '/login?session=expired';
    }
    return Promise.reject(error);
  }
);
```

### 2.3 Authentication State Management

#### 2.3.1 React Context (Client-Side)

**AuthContext** (`src/lib/context/AuthContext.tsx`)
- **Purpose**: Provide authentication state to all React components
- **State Shape**:
  ```typescript
  {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;  // for initial token validation
    error: string | null;
  }
  ```

**Context Methods**:
```typescript
interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  login(username: string, password: string): Promise<void>;
  register(userData: RegisterRequest): Promise<void>;
  logout(): void;

  // Token helpers
  getAuthToken(): string | null;
}
```

**Provider Implementation**:
- On mount: Check for existing token via `tokenService.getToken()`
- If token exists and valid: Fetch user profile or decode JWT for user data
- Set loading state during initialization
- Expose methods that call auth service and update local state

**Usage in Components**:
```typescript
const { isAuthenticated, user, login, logout } = useAuth();
```

#### 2.3.2 Astro Page Guards (Server-Side)

**Route Protection Pattern** (used in protected pages like `/generate`, `/flashcards`)

**Server-Side Check** (in Astro page frontmatter):
```typescript
// In protected page like generate.astro
---
import { checkAuth } from '../lib/server/auth';

const authResult = checkAuth(Astro.cookies, Astro.request);
if (!authResult.isAuthenticated) {
  return Astro.redirect('/login?redirect=' + Astro.url.pathname);
}

const user = authResult.user;
---
```

**Auth Check Module** (`src/lib/server/auth.ts`)
- **Purpose**: Server-side authentication validation for Astro pages
- **Approach**:
  - Read token from cookie (set by client after login)
  - Optionally validate JWT signature (if secret is available server-side)
  - For MVP: Check token existence and basic expiration
  - Return user data if authenticated

**Functions**:
```typescript
checkAuth(cookies: AstroCookies, request: Request): AuthCheckResult
  - Reads auth cookie
  - Validates token format and expiration
  - Returns: { isAuthenticated: boolean, user?: User }

setAuthCookie(cookies: AstroCookies, token: string, expiresIn: number): void
  - Sets secure, httpOnly cookie for token
  - Used after successful login

clearAuthCookie(cookies: AstroCookies): void
  - Removes auth cookie
  - Used on logout
```

**Cookie Strategy**:
- Cookie name: `auth_token`
- Attributes: `httpOnly=false` (client needs access), `secure=true` (HTTPS only), `sameSite=strict`
- Alternative: Use separate cookie for server-side (httpOnly=true) and localStorage for client-side

### 2.4 User Profile Management

#### 2.4.1 User Profile Data

**User Data Structure** (already defined in OpenAPI):
```typescript
interface User {
  id: string;           // UUID
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}
```

**Data Source**:
- After login: JWT token may contain user claims (username, id)
- Alternative: Call `GET /users/me` endpoint (if implemented) to fetch full profile
- For MVP: Decode JWT on client-side to extract user info

**JWT Decoding**:
- Use library like `jwt-decode` (client-side only)
- Extract user claims: `{ sub: userId, username, email, firstName, lastName }`
- Store decoded user object in AuthContext state

#### 2.4.2 User Display

**Navigation Display**:
- Show user's first name and last name in header: "Welcome, John Doe"
- Or: Show username with icon
- Include logout button

**No Profile Editing in MVP**:
- User profile editing is out of scope for US-001 and US-002
- Display only, no edit functionality

---

## 3. INTEGRATION WITH EXISTING FEATURES

### 3.1 AI Flashcard Generation Flow (No Changes)

**Current State** (`/generate` page):
- User accesses generate page
- React app (`GenerateApp`) loads with `GenerationContext`
- User inputs text and generates flashcards
- API calls use `apiClient` from `src/lib/api/client.ts`

**After Authentication Integration**:
- Page becomes protected (requires authentication)
- User must be logged in to access
- JWT token automatically added to API requests via axios interceptor
- No changes to React components or generation logic
- Backend associates flashcards with authenticated user (via JWT)

**Impact**:
- Zero changes to existing generation components
- Zero changes to generation workflow/logic
- Only change: Page access control at Astro level

### 3.2 Flashcards Management (Future)

**Current State** (`/flashcards` page):
- Stub page with placeholder text
- Not yet implemented

**After Authentication Integration**:
- Page becomes protected
- Future implementation will use authenticated API calls
- API endpoints (`GET /flashcards`, `PUT /flashcards/{id}`, etc.) already expect JWT
- Implementation will use same authentication patterns

**Impact**:
- Future implementation will follow established auth patterns
- No retroactive changes needed

### 3.3 API Client Compatibility

**Current API Client** (`src/lib/api/client.ts`):
- Already configured with base URL
- Has interceptor stubs for auth
- Used by AI generation API service (`src/lib/api/ai.ts`)

**Changes Needed**:
- Complete request interceptor to add JWT token
- Enhance response interceptor for 401 handling
- No changes to function signatures or return types
- Existing API services continue to work unchanged

**Backward Compatibility**:
- All existing API calls (`createSession`, `getSession`, `getSuggestions`, `approveSession`) continue to work
- Token addition is transparent to calling code
- No refactoring of existing API service functions

---

## 4. TECHNICAL MODULES & CONTRACTS

### 4.1 Module Inventory

#### 4.1.1 New Modules

**Pages**:
- `src/pages/register.astro` - Registration page
- `src/pages/login.astro` - Login page

**React Components**:
- `src/components/auth/RegisterForm.tsx` - Registration form
- `src/components/auth/LoginForm.tsx` - Login form
- `src/components/auth/AuthLayout.tsx` - Wrapper layout for auth pages
- `src/components/navigation/AppHeader.tsx` - Navigation with auth state

**Services**:
- `src/lib/api/auth.ts` - Authentication API service
- `src/lib/services/tokenService.ts` - JWT token management
- `src/lib/server/auth.ts` - Server-side auth helpers for Astro

**Context**:
- `src/lib/context/AuthContext.tsx` - React authentication context

**Validation**:
- `src/lib/validation/auth.ts` - Authentication form validation

**Types**:
- Extend `src/lib/api/types.ts` with auth-related interfaces

**Utilities**:
- `src/lib/utils/jwt.ts` - JWT decoding and validation utilities

#### 4.1.2 Modified Modules

**API Client**:
- `src/lib/api/client.ts` - Complete auth interceptors

**Layout**:
- `src/layouts/Layout.astro` - Add navigation support and auth guards

**Pages**:
- `src/pages/index.astro` - Add auth-aware landing page
- `src/pages/generate.astro` - Add auth guard
- `src/pages/flashcards.astro` - Add auth guard

**Components**:
- `src/components/Welcome.astro` - Add links to register/login for unauthenticated users

### 4.2 Service Contracts

#### 4.2.1 Authentication Service Contract

**Module**: `src/lib/api/auth.ts`

**Interface**:
```typescript
export interface IAuthService {
  register(data: RegisterRequest): Promise<RegisterResponse>;
  login(credentials: LoginRequest): Promise<LoginResponse>;
  logout(): void;
}
```

**Dependencies**:
- `apiClient` from `src/lib/api/client.ts`
- `tokenService` from `src/lib/services/tokenService.ts`

**Error Handling**:
- Throws `APIError` with status code and message
- Calling code handles error display

#### 4.2.2 Token Service Contract

**Module**: `src/lib/services/tokenService.ts`

**Interface**:
```typescript
export interface ITokenService {
  setToken(token: string, expiresIn: number): void;
  getToken(): string | null;
  isTokenValid(): boolean;
  clearToken(): void;
  getTimeUntilExpiration(): number | null;
}
```

**Storage**:
- Uses browser localStorage
- Keys: `auth_token`, `auth_token_expiry`

**No Dependencies**: Pure utility service

#### 4.2.3 Validation Service Contract

**Module**: `src/lib/validation/auth.ts`

**Interface**:
```typescript
export interface IAuthValidation {
  validateUsername(username: string): ValidationResult;
  validatePassword(password: string): ValidationResult;
  validateEmail(email: string): ValidationResult;
  validateName(name: string, fieldName: string): ValidationResult;
  validateRegistrationForm(data: RegisterRequest): FormValidationResult;
  validateLoginForm(data: LoginRequest): FormValidationResult;
}

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

export interface FormValidationResult {
  isValid: boolean;
  fieldErrors: Record<string, string[]>;
}
```

**No Dependencies**: Pure validation logic

#### 4.2.4 Server Auth Service Contract

**Module**: `src/lib/server/auth.ts`

**Interface**:
```typescript
export interface IServerAuth {
  checkAuth(cookies: AstroCookies, request: Request): AuthCheckResult;
  setAuthCookie(cookies: AstroCookies, token: string, expiresIn: number): void;
  clearAuthCookie(cookies: AstroCookies): void;
}

export interface AuthCheckResult {
  isAuthenticated: boolean;
  user?: User;
  error?: string;
}
```

**Dependencies**:
- Astro's cookie API
- Optional: JWT validation library for server-side verification

### 4.3 Component Contracts

#### 4.3.1 RegisterForm Component

**Location**: `src/components/auth/RegisterForm.tsx`

**Props**: None (self-contained)

**Internal State**:
```typescript
{
  formData: RegisterRequest;
  errors: Record<string, string[]>;
  isSubmitting: boolean;
  apiError: string | null;
}
```

**Dependencies**:
- `useAuth()` hook from AuthContext
- `authValidation` service
- Shadcn/ui components (Input, Button, Label, Alert)

**Emits**:
- No events (uses context for state updates)

**Side Effects**:
- Calls `authContext.register()`
- Navigates to `/generate` on success

#### 4.3.2 LoginForm Component

**Location**: `src/components/auth/LoginForm.tsx`

**Props**:
```typescript
{
  redirectTo?: string;  // URL to redirect after successful login
}
```

**Internal State**:
```typescript
{
  username: string;
  password: string;
  errors: Record<string, string>;
  isSubmitting: boolean;
  apiError: string | null;
}
```

**Dependencies**:
- `useAuth()` hook from AuthContext
- `authValidation` service
- Shadcn/ui components

**Side Effects**:
- Calls `authContext.login()`
- Navigates to `redirectTo` or `/generate` on success

#### 4.3.3 AuthContext Provider

**Location**: `src/lib/context/AuthContext.tsx`

**Props**:
```typescript
{
  children: React.ReactNode;
}
```

**Context Value**:
```typescript
{
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  getAuthToken: () => string | null;
}
```

**Dependencies**:
- `authService` (API calls)
- `tokenService` (token management)
- JWT decode utility (for extracting user from token)

**Lifecycle**:
- On mount: Load token and validate
- On login/register: Store token and update state
- On logout: Clear token and state

#### 4.3.4 AppHeader/Navigation Component

**Location**: `src/components/navigation/AppHeader.tsx`

**Props**:
```typescript
{
  showAuthButtons?: boolean;  // default: true
}
```

**Dependencies**:
- `useAuth()` hook from AuthContext (if React component)
- Or receives auth state as prop from Astro page

**Conditional Rendering**:
- If authenticated: Show "Generate", "Flashcards", user name, "Logout"
- If not authenticated: Show "Login", "Register"

### 4.4 Data Flow Diagrams

#### 4.4.1 Registration Flow

```
User (RegisterForm)
  → Enter form data
  → Client-side validation (authValidation)
  → Click "Register"
  → authContext.register(formData)
    → authService.register(formData)
      → apiClient.post('/auth/register')
        → Backend validates and creates user
        → Returns userId (UUID)
      → Success: Auto-login flow
        → authService.login(username, password)
          → apiClient.post('/auth/login')
            → Backend returns { username, accessToken, expiresIn }
          → tokenService.setToken(accessToken, expiresIn)
          → authContext updates state (user, isAuthenticated)
      → Frontend redirects to /generate
```

#### 4.4.2 Login Flow

```
User (LoginForm)
  → Enter credentials
  → Click "Login"
  → authContext.login(username, password)
    → authService.login(credentials)
      → apiClient.post('/auth/login')
        → Backend validates credentials
        → Returns { username, accessToken, expiresIn }
      → tokenService.setToken(accessToken, expiresIn)
      → Decode JWT to extract user data
      → authContext updates state (user, isAuthenticated)
    → Frontend redirects to /generate
```

#### 4.4.3 Protected API Call Flow

```
User action (e.g., generate flashcards)
  → React component calls API service
    → apiService.createSession(text)
      → apiClient.post('/ai/sessions', { inputText })
        → [Request Interceptor]
          → tokenService.getToken()
          → Add Authorization: Bearer {token}
        → Backend receives request with JWT
        → Backend validates JWT
        → Success: Returns session data
      → OR Backend returns 401 Unauthorized
        → [Response Interceptor]
          → Detect 401
          → tokenService.clearToken()
          → authContext.logout()
          → Redirect to /login?session=expired
```

#### 4.4.4 Page Guard Flow (Server-Side)

```
User navigates to /generate
  → Astro server processes request
    → Page frontmatter runs
      → checkAuth(Astro.cookies, Astro.request)
        → Read auth_token cookie
        → Validate token exists and not expired
        → Return { isAuthenticated: boolean, user?: User }
      → If not authenticated:
        → return Astro.redirect('/login?redirect=/generate')
      → If authenticated:
        → Render page normally
        → Pass user data to React components via props/context
```

---

## 5. SECURITY CONSIDERATIONS

### 5.1 Token Storage

**Strategy**: localStorage + cookie
- **localStorage**: Client-side access for React components and axios
- **Cookie**: Server-side validation for Astro page guards
- **Risk**: localStorage vulnerable to XSS attacks
- **Mitigation**:
  - Sanitize all user inputs
  - Use Content Security Policy (CSP) headers
  - Future: Migrate to httpOnly cookies with separate API for token refresh

### 5.2 Password Security

**Client-Side**:
- Passwords never logged or exposed in errors
- Password fields use `type="password"`
- Password validation ensures strong passwords
- Passwords cleared from form state on submission

**Backend** (already implemented):
- Passwords hashed with bcrypt or similar
- Never stored in plain text
- Never returned in API responses

### 5.3 HTTPS Requirement

**Production**:
- All authentication flows require HTTPS
- Cookies set with `secure=true` flag
- Tokens never transmitted over HTTP

**Development**:
- localhost HTTP acceptable
- Environment flag to disable secure cookie in dev

### 5.4 Token Expiration

**Token Lifecycle**:
- JWT has expiration time (`expiresIn` from login response)
- Frontend stores expiration timestamp
- Frontend validates expiration before each request
- Expired tokens automatically cleared
- User redirected to login

**Auto-Logout** (Optional Enhancement):
- Set timer based on `expiresIn`
- Show warning before expiration: "Session expiring in 2 minutes"
- Auto-logout and redirect when expired

### 5.5 CORS Configuration

**Backend CORS** (assumed configured):
- Allow frontend origin (e.g., `http://localhost:3000`)
- Allow credentials (for cookies)
- Restrict to specific origins in production

**Frontend**:
- axios configured with `withCredentials: true` if using cookies

---

## 6. TESTING STRATEGY

### 6.1 Component Testing

**RegisterForm**:
- Unit test: Validation logic
- Integration test: Form submission with mock API
- Test cases:
  - Valid registration succeeds
  - Invalid fields show errors
  - Duplicate username shows conflict error
  - Network error handled gracefully

**LoginForm**:
- Unit test: Required field validation
- Integration test: Login flow with mock API
- Test cases:
  - Valid login succeeds
  - Invalid credentials show error
  - Network error handled
  - Redirect to return URL after login

### 6.2 Service Testing

**authService**:
- Mock axios calls
- Test success and error responses
- Verify token storage after login
- Verify token cleared on logout

**tokenService**:
- Test token storage and retrieval
- Test expiration validation
- Test token clearing
- Test localStorage interaction

**validationService**:
- Unit test all validation functions
- Test edge cases (empty, too long, special chars)
- Test form-level validation

### 6.3 Integration Testing

**Registration to Login Flow**:
- Register new user
- Verify redirect to protected page
- Verify token stored
- Verify API calls include token

**Logout and Re-login**:
- Login, logout, verify token cleared
- Verify protected pages redirect to login
- Login again, verify access restored

**Token Expiration**:
- Mock expired token
- Attempt protected API call
- Verify redirect to login
- Verify session expired message

### 6.4 E2E Testing

**Full User Journey**:
1. Visit landing page
2. Click register
3. Fill form and submit
4. Redirected to /generate
5. Generate flashcards (API call with token)
6. Navigate to /flashcards
7. Logout
8. Verify redirected to login
9. Login again
10. Verify access to /generate

---

## 7. FUTURE ENHANCEMENTS

### 7.1 Password Recovery

**Out of Scope for US-001/US-002**:
- "Forgot password" link on login page
- Email-based password reset flow
- Backend endpoint: `POST /auth/forgot-password`
- Requires email service integration

### 7.2 Email Verification

**Not Required for MVP**:
- Email confirmation after registration
- Backend sends verification email
- User clicks link to activate account

### 7.3 Remember Me

**Optional Feature**:
- Checkbox on login form
- Extends token expiration to 30 days
- Uses longer-lived refresh token

### 7.4 Social Login

**Future Integration**:
- Login with Google, GitHub, etc.
- OAuth 2.0 flow
- Backend integration with OAuth providers

### 7.5 Two-Factor Authentication

**Security Enhancement**:
- TOTP-based 2FA
- SMS-based 2FA
- Backup codes

### 7.6 Account Management

**User Profile Page**:
- Edit profile information
- Change password
- Delete account (US-009 requirement, separate implementation)

---

## 8. IMPLEMENTATION PHASES

### Phase 1: Core Authentication (US-001, US-002)
**Deliverables**:
- Registration page and form
- Login page and form
- AuthContext and token management
- API service integration
- Protected route guards
- Navigation with auth state

**Acceptance Criteria**:
- User can register with valid data
- User receives confirmation and is logged in
- User can log in with correct credentials
- Invalid credentials show error message
- Protected pages require authentication
- Token included in API requests
- User can log out

### Phase 2: Error Handling & Validation
**Deliverables**:
- Comprehensive client-side validation
- Field-level error messages
- API error handling
- Session expiration handling

**Acceptance Criteria**:
- All validation rules enforced
- Clear error messages displayed
- Network errors handled gracefully
- Expired sessions redirect to login

### Phase 3: UI/UX Polish
**Deliverables**:
- Loading states and spinners
- Success messages
- Accessibility improvements (ARIA labels, focus management)
- Responsive design for mobile

**Acceptance Criteria**:
- Forms show loading state during submission
- Success feedback clear and immediate
- All interactive elements keyboard accessible
- Forms work on mobile devices

### Phase 4: Testing & Documentation
**Deliverables**:
- Unit tests for components and services
- Integration tests for auth flows
- E2E tests for user journeys
- Developer documentation

**Acceptance Criteria**:
- 80%+ code coverage for auth modules
- All critical paths tested
- Documentation complete and accurate

---

## 9. COMPATIBILITY CHECKLIST

### 9.1 Existing Features Preserved

- **AI Generation Workflow**: No changes to generation logic, context, or components
- **API Client**: Enhanced with auth, but existing calls continue to work
- **Flashcards Page**: Prepared for future implementation with auth
- **Layouts**: Extended but backward compatible
- **Styling**: No changes to Tailwind config or global styles
- **Build Process**: No changes to build or deployment

### 9.2 New Requirements Satisfied

- **US-001 Registration**: Form with email, password, name fields; activation; confirmation
- **US-002 Login**: Credentials form; secure storage; redirect to generation view; error messages
- **US-009 Authorization**: Only logged-in users access flashcards; no sharing

### 9.3 Non-Breaking Changes

- All existing pages continue to function
- No changes to existing component props or APIs
- Optional authentication (can be disabled via feature flag for testing)
- Progressive enhancement: Auth adds security without removing functionality

---

## SUMMARY

This specification defines a comprehensive authentication system that:

1. **Implements US-001 and US-002** with registration and login functionality
2. **Integrates seamlessly** with existing AI generation and flashcard features
3. **Uses established patterns** from the Astro + React stack
4. **Provides clear contracts** for all services, components, and modules
5. **Ensures security** through JWT tokens, validation, and protected routes
6. **Maintains compatibility** with all existing application behavior
7. **Enables future features** like profile management and password recovery
8. **Follows project conventions** for component structure, styling, and TypeScript usage

The architecture balances **simplicity** for MVP with **extensibility** for future enhancements, using industry-standard patterns for authentication in static site generators with client-side interactivity.
