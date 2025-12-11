# Authentication Feature

## Summary

The `auth` feature module handles user authentication for the Stock app, providing login and signup
functionality with JWT-based authentication. It follows the MVVM architecture pattern with clear
separation between data, UI, and ViewModel layers.

### Key Features

- **Login**: Email/password authentication with JWT token storage
- **Signup**: New user registration with password confirmation
- **Token Management**: Dual-layer token storage (in-memory + persistent DataStore)
- **Input Validation**: Email format, password length, and password match validation
- **Error Handling**: User-friendly error messages for various failure scenarios

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                   │
│  ┌─────────────────────┐         ┌─────────────────────┐                │
│  │    LoginScreen      │         │    SignupScreen     │                │
│  │  ┌───────────────┐  │         │  ┌───────────────┐  │                │
│  │  │ LoginUiState  │  │         │  │ SignupUiState │  │                │
│  │  └───────────────┘  │         │  └───────────────┘  │                │
│  └──────────┬──────────┘         └──────────┬──────────┘                │
└─────────────┼───────────────────────────────┼───────────────────────────┘
              │ observes                      │ observes
              ▼                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel Layer                               │
│  ┌─────────────────────┐         ┌─────────────────────┐                │
│  │   LoginViewModel    │         │   SignupViewModel   │                │
│  │  - ui: StateFlow    │         │  - ui: StateFlow    │                │
│  │  - events: SharedFlow│        │  - events: SharedFlow│               │
│  └──────────┬──────────┘         └──────────┬──────────┘                │
│             │                               │                           │
│             │ uses                          │ uses                      │
│             ▼                               ▼                           │
│  ┌─────────────────────┐         ┌─────────────────────┐                │
│  │   InputValidator    │         │    ErrorHandler     │                │
│  │  - validateLogin()  │         │  - logError()       │                │
│  │  - validateSignup() │         │  - mapErrorToResource()│             │
│  └─────────────────────┘         └─────────────────────┘                │
└─────────────┬───────────────────────────────┬───────────────────────────┘
              │ calls                         │
              └───────────────┬───────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository Layer                               │
│                    ┌─────────────────────┐                              │
│                    │   AuthRepository    │                              │
│                    │  - login()          │                              │
│                    │  - signup()         │                              │
│                    │  - logout()         │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│     AuthApi     │  │  TokenProvider  │  │   TokenStore    │
│   (Retrofit)    │  │  (In-memory)    │  │  (DataStore)    │
│  - login()      │  │  - update()     │  │  - save()       │
│  - signup()     │  │  - clear()      │  │  - clear()      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
        │                    │                    │
        │                    └────────┬───────────┘
        ▼                             ▼
┌─────────────────┐         ┌─────────────────────┐
│   Backend API   │         │    core module      │
│  POST /login    │         │ (shared components) │
│  POST /signup   │         └─────────────────────┘
└─────────────────┘
```

## Directory Structure

```
feature/auth/
├── data/
│   ├── remote/
│   │   ├── AuthApi.kt           # Retrofit interface for login/signup endpoints
│   │   └── AuthModels.kt        # DTOs: LoginRequest, LoginResponse,
│   │                            #       SignupRequest, SignupResponse
│   └── repository/
│       └── AuthRepository.kt    # Coordinates API calls and token management
│
├── ui/
│   ├── login/
│   │   ├── LoginScreen.kt       # Login composable with Hilt ViewModel injection
│   │   └── LoginUiState.kt      # Login screen state data class
│   └── signup/
│       ├── SignupScreen.kt      # Signup composable with Hilt ViewModel injection
│       └── SignupUiState.kt     # Signup screen state data class
│
├── viewmodel/
│   ├── LoginViewModel.kt        # Manages login state and business logic
│   ├── SignupViewModel.kt       # Manages signup state and business logic
│   ├── InputValidator.kt        # Reusable input validation utilities
│   └── ErrorHandler.kt          # Error logging and mapping utilities
│
└── README.md                    # This file
```

## Testing

### Test Location

Tests are organized by type:

| Test Type  | Location                                                   |
|------------|------------------------------------------------------------|
| Unit Tests | `app/src/test/java/com/example/stock/`                     |
| UI Tests   | `app/src/androidTest/java/com/example/stock/feature/auth/` |

### Test Files

#### Unit Tests

| File                                    | Description                                                                               |
|-----------------------------------------|-------------------------------------------------------------------------------------------|
| `viewmodel/LoginViewModelTest.kt`       | Tests for LoginViewModel including validation, login success/failure, and logout          |
| `viewmodel/SignupViewModelTest.kt`      | Tests for SignupViewModel including validation, signup success/failure, and error mapping |
| `data/repository/AuthRepositoryTest.kt` | Tests for AuthRepository including token management                                       |

#### UI Tests (Instrumented)

| File                                              | Description                                 |
|---------------------------------------------------|---------------------------------------------|
| `feature/auth/ui/login/LoginScreenContentTest.kt` | Compose UI tests for LoginScreen components |

### Running Tests

```bash
# Run all unit tests for auth feature
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.SignupViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.AuthRepositoryTest.*"

# Run UI tests (requires emulator/device)
./gradlew connectedAndroidTest --tests "*.LoginScreenContentTest.*"
```

### Test Coverage

#### LoginViewModel Tests

- Input state updates (`onEmailChange`, `onPasswordChange`, `togglePassword`)
- Validation (empty fields, invalid email, short password)
- Login success/failure handling
- Loading state management
- Logout functionality

#### SignupViewModel Tests

- Input state updates (email, password, confirm password)
- Validation (empty fields, invalid email, short password, password mismatch)
- Signup success/failure handling
- HTTP 409 conflict handling (email already registered)
- Loading state management

#### AuthRepository Tests

- Login flow with token persistence
- Token provider and store coordination
- Logout with token clearing

#### LoginScreenContent UI Tests

- Initial state display
- Input field interactions
- Button click callbacks
- Loading state (button disabled)
- Error message display
- Password visibility toggle

### Testing Patterns

The feature uses the following testing patterns:

1. **MainDispatcherRule**: Replaces `Dispatchers.Main` with `StandardTestDispatcher` for
   deterministic coroutine testing
2. **TestDispatcherProvider**: Provides test dispatchers for ViewModel testing
3. **MockK**: Mocking framework with `relaxed = true` for simple mocks
4. **Truth**: Assertion library using `assertThat()` style
5. **Compose Test Rule**: For UI testing with `createComposeRule()`

Example test pattern:

```kotlin
@Test
fun `login success - emits LoggedIn event`() = runTest(mainRule.scheduler) {
        // Arrange
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } returns LoginResponse("token")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.ui.value.isLoading).isFalse()
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }
```