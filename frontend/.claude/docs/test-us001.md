# Test Plan: US-001 Account Registration

## User Story
**ID**: US-001
**Title**: Account registration
**Description**: As a new user, I want to register so I can access my own flashcards and use AI flashcard generation.

## Acceptance Criteria
- Registration form contains fields for email address and password.
- After correctly filling out the form and data verification, the account is activated.
- User receives confirmation of successful registration and is logged in.

---

## Test Cases

| Test Case ID | Test Name | Short Description | Input | Expected Output |
|-------------|-----------|-------------------|-------|-----------------|
| **TC-001-001** | Successful registration with valid data | Verify user can register with all valid data | Username: `testuser123`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Registration succeeds<br/>- Success message displayed: "Registration successful! Redirecting..."<br/>- User is auto-logged in<br/>- User is redirected to `/generate` page<br/>- JWT token stored in localStorage and cookies |
| **TC-001-002** | Registration form is accessible | Verify registration page loads correctly | Navigate to `/register` | - Page loads successfully<br/>- Form displays all required fields: username, email, firstName, lastName, password<br/>- "Register" button is visible<br/>- Link to login page is visible |
| **TC-001-003** | Registration with minimum valid username | Verify registration works with 3-character username | Username: `abc`<br/>Password: `Test@1234`<br/>Email: `min@example.com`<br/>First Name: `A`<br/>Last Name: `B` | - Registration succeeds<br/>- User is redirected to `/generate` |
| **TC-001-004** | Registration with maximum valid username | Verify registration works with 50-character username | Username: `a` repeated 50 times<br/>Password: `Test@1234`<br/>Email: `max@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Registration succeeds<br/>- User is redirected to `/generate` |
| **TC-001-005** | Registration with special characters in name | Verify names can contain various characters | Username: `user123`<br/>Password: `Test@1234`<br/>Email: `special@example.com`<br/>First Name: `Mary-Jane`<br/>Last Name: `O'Connor` | - Registration succeeds<br/>- User is redirected to `/generate` |
| **TC-001-006** | Empty form submission | Verify validation for all required fields | All fields empty | - Form does not submit<br/>- Error messages displayed:<br/>  * "Username is required"<br/>  * "Password is required"<br/>  * "Email is required"<br/>  * "First name is required"<br/>  * "Last name is required" |
| **TC-001-007** | Username too short | Verify minimum username length validation | Username: `ab` (2 chars)<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Username must be at least 3 characters" |
| **TC-001-008** | Username too long | Verify maximum username length validation | Username: 51 characters<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Username must not exceed 50 characters" |
| **TC-001-009** | Username with invalid characters | Verify username pattern validation | Username: `test@user!`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Username must contain only letters, numbers, and underscore" |
| **TC-001-010** | Password too short | Verify minimum password length | Username: `testuser`<br/>Password: `Test@12` (7 chars)<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must be at least 8 characters" |
| **TC-001-011** | Password too long | Verify maximum password length | Username: `testuser`<br/>Password: 101 characters<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must not exceed 100 characters" |
| **TC-001-012** | Password without uppercase letter | Verify password complexity - uppercase | Username: `testuser`<br/>Password: `test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must contain at least one uppercase letter" |
| **TC-001-013** | Password without lowercase letter | Verify password complexity - lowercase | Username: `testuser`<br/>Password: `TEST@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must contain at least one lowercase letter" |
| **TC-001-014** | Password without number | Verify password complexity - number | Username: `testuser`<br/>Password: `Test@abcd`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must contain at least one number" |
| **TC-001-015** | Password without special character | Verify password complexity - special char | Username: `testuser`<br/>Password: `Test1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Password must contain at least one special character" |
| **TC-001-016** | Invalid email format | Verify email validation | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `notanemail`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Please enter a valid email address" |
| **TC-001-017** | Email too long | Verify maximum email length | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `a` repeated 100+ times + `@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "Email must not exceed 100 characters" |
| **TC-001-018** | First name too long | Verify maximum first name length | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: 51 characters<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "First name must not exceed 50 characters" |
| **TC-001-019** | Last name too long | Verify maximum last name length | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: 51 characters | - Form does not submit<br/>- Error displayed: "Last name must not exceed 50 characters" |
| **TC-001-020** | Empty first name | Verify first name is required | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: ` ` (empty)<br/>Last Name: `Doe` | - Form does not submit<br/>- Error displayed: "First name is required" |
| **TC-001-021** | Empty last name | Verify last name is required | Username: `testuser`<br/>Password: `Test@1234`<br/>Email: `test@example.com`<br/>First Name: `John`<br/>Last Name: ` ` (empty) | - Form does not submit<br/>- Error displayed: "Last name is required" |
| **TC-001-022** | Duplicate username | Verify username uniqueness | Username: `existinguser` (already registered)<br/>Password: `Test@1234`<br/>Email: `new@example.com`<br/>First Name: `John`<br/>Last Name: `Doe` | - Registration fails<br/>- Error message displayed: "This username or email is already registered. Please try another." |
| **TC-001-023** | Duplicate email | Verify email uniqueness | Username: `newuser`<br/>Password: `Test@1234`<br/>Email: `existing@example.com` (already used)<br/>First Name: `John`<br/>Last Name: `Doe` | - Registration fails<br/>- Error message displayed: "This username or email is already registered. Please try another." |
| **TC-001-024** | Real-time validation on blur | Verify field validation triggers on blur | Focus on username field, enter `ab`, blur field | - Error message appears immediately below username field<br/>- Error: "Username must be at least 3 characters" |
| **TC-001-025** | Error clears on valid input | Verify error messages clear when input becomes valid | Enter invalid username `ab`, blur (error appears), then type `abc` | - Error message disappears when valid input is entered |
| **TC-001-026** | Form disabled during submission | Verify form elements disable while submitting | Fill valid data, click Register | - Submit button shows "Registering..."<br/>- All form fields are disabled<br/>- Form cannot be resubmitted |
| **TC-001-027** | Backend unavailable | Verify handling when backend is down | Fill valid data, submit when backend is offline | - Error message displayed: "Unable to connect to server. Please check your connection."<br/>- Form fields remain enabled<br/>- User can try again |
| **TC-001-028** | Network timeout | Verify handling of slow/timeout network | Fill valid data, submit with network delay | - Appropriate error handling<br/>- Error message: "Registration failed. Please try again later." |
| **TC-001-029** | Already authenticated user redirected | User already logged in navigates to /register | User is logged in, navigates to `/register` | - User is automatically redirected to `/generate`<br/>- Registration form is not shown |
| **TC-001-030** | Link to login page | Verify link to login page works | Click "Login" link at bottom of registration form | - User is redirected to `/login` page |
| **TC-001-031** | Password field masking | Verify password is masked in input | Type password in password field | - Characters are displayed as dots/asterisks<br/>- Password is not visible in plain text |
| **TC-001-032** | Form retains data on error | Verify form data persists after validation error | Fill all fields with one invalid, submit, get error | - All fields retain their values (except password)<br/>- User doesn't need to re-enter all data |

---

## Test Data Requirements

### Valid Test Users
- `testuser001` - `Test@1234` - `testuser001@example.com` - `John` - `Doe`
- `testuser002` - `SecureP@ss123` - `testuser002@example.com` - `Jane` - `Smith`
- `abc` - `ValidP@ss1` - `min@example.com` - `A` - `B`

### Invalid Usernames
- `ab` (too short)
- `a` repeated 51 times (too long)
- `test@user` (invalid characters)
- `user name` (contains space)

### Invalid Passwords
- `short1` (too short)
- `nouppercas3!` (no uppercase)
- `NOLOWERCASE3!` (no lowercase)
- `NoNumbers!` (no number)
- `NoSpecial123` (no special char)

### Invalid Emails
- `notanemail` (no @ or domain)
- `missing@domain` (no TLD)
- `@example.com` (no local part)
- `user@` (no domain)

---

## Test Execution Notes

1. **Test Environment**: E2E tests should run against dockerized backend with fresh database
2. **Test Isolation**: Each test should use unique username/email to avoid conflicts
3. **Data Cleanup**: Use `docker compose down -v` to reset database between full test runs
4. **Browser Coverage**: Tests should run on Chromium, Firefox, and WebKit
5. **Accessibility**: Ensure error messages have proper ARIA attributes for screen readers
