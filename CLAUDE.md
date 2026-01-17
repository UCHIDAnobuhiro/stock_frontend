# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

Android stock chart viewer app using Kotlin + Jetpack Compose + MVVM architecture. Displays US stock
candlestick charts (daily/weekly/monthly) with JWT authentication and a Go backend API.

## Common Commands

### Build & Run

```bash
# Build the project
./gradlew build

# Build debug variant
./gradlew assembleDebug

# Build staging variant
./gradlew assembleStaging

# Build release variant
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Testing

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run tests for a specific class
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.LoginViewModelTest"

# Run tests with coverage
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint
```

### Clean & Sync

```bash
# Clean build artifacts
./gradlew clean

# Sync project dependencies
./gradlew --no-daemon --stacktrace help
```

## Architecture

### MVVM Pattern

- **UI Layer** (Compose): Screens observe ViewModel state via StateFlow/SharedFlow
- **ViewModel Layer**: Manages UI state and business logic, exposes UiState classes
- **Repository Layer**: Coordinates data sources and transforms DTOs to UI models
- **DataSource Layer**: API calls (Retrofit) and local storage (DataStore)

### Key Architectural Patterns

**UiState Pattern**: ViewModels expose domain models via dedicated UiState classes (not DTOs
directly). Example: `CandleUiState`, `SymbolUiState`, `LoginUiState`.

**Hilt DI**: ViewModels use Hilt for dependency injection with `@HiltViewModel` and `@Inject constructor`. Dependencies like repositories and `DispatcherProvider` are injected automatically.

**Token Management**: Dual-layer approach with `InMemoryTokenProvider` (in-memory) + `TokenStore` (
DataStore persistence). `AuthInterceptor` automatically adds JWT Bearer token to API requests.

### Navigation Flow

1. `LoginScreen` (Routes.LOGIN) → validates email/password → calls `LoginViewModel.login()`
2. On success → navigates to `StockListScreen` (Routes.STOCK) with back stack cleared
3. Selecting a stock → navigates to `chart/{name}/{code}` with URL parameters
4. Logout from any screen → clears tokens → navigates back to login

### Chart Synchronization

The app uses MPAndroidChart with custom synchronization between candlestick and volume charts:

- `ChartSync.kt` implements bidirectional zoom/scroll/highlight synchronization using
  `AtomicBoolean` locks to prevent infinite loops
- Inertia scrolling is disabled (`isDragDecelerationEnabled = false`) to prevent lag
- See `attachSynchronizedPair()` function in `ui/chart/ChartSync.kt`

### API Configuration

- **Debug build**: `BASE_URL = "http://10.0.2.2:8080/"` (Android emulator localhost)
- **Staging build**: `BASE_URL = "https://api.stockviewapp.com/"`
- **Release build**: `BASE_URL = "https://api.stockviewapp.com/"`
- BASE_URL is exposed via `BuildConfig.BASE_URL` and accessed through `ApiConfig`

### Testing Strategy

- Uses `MainDispatcherRule` to replace `Dispatchers.Main` with `StandardTestDispatcher` for
  deterministic coroutine testing
- MockK for mocking dependencies (with `relaxed = true` for simple cases)
- Truth library for assertions (`assertThat()`)
- Pattern: mock repository → inject into ViewModel → verify state changes via `ui.value`
- Always call `advanceUntilIdle()` after async operations in tests

## CI/CD

GitHub Actions runs on pull requests to `main`:

- Sets up JDK 17
- Runs `./gradlew lint`
- Runs `./gradlew testDebugUnitTest`
- Uploads test and lint reports as artifacts

## Package Structure

This project uses a **feature-based** package structure where related components (data, UI, ViewModel) are grouped by feature rather than by layer. This improves modularity and makes it easier to understand and modify features independently.

```
com.example.stock/
├── feature/
│   ├── auth/                    # Authentication feature
│   │   ├── data/
│   │   │   ├── remote/          # AuthApi (Retrofit), AuthModels (Request/Response DTOs)
│   │   │   └── repository/      # AuthRepository
│   │   ├── ui/
│   │   │   ├── login/           # LoginScreen, LoginUiState
│   │   │   └── signup/          # SignupScreen, SignupUiState
│   │   └── viewmodel/           # LoginViewModel, LogoutViewModel, SignupViewModel
│   ├── stocklist/               # Stock list feature
│   │   ├── data/
│   │   │   ├── remote/          # SymbolApi (Retrofit), SymbolDto
│   │   │   └── repository/      # SymbolRepository
│   │   ├── ui/                  # SymbolListScreen, SymbolUiState, SymbolItem
│   │   └── viewmodel/           # SymbolViewModel
│   └── chart/                   # Chart display feature
│       ├── data/
│       │   ├── remote/          # ChartApi (Retrofit), CandleDto
│       │   └── repository/      # CandleRepository
│       ├── ui/                  # ChartScreen, CandleUiState, CandleItem
│       │   └── chart/           # CandleChartView, VolumeChartView, ChartSync
│       └── viewmodel/           # CandlesViewModel
├── core/
│   ├── data/                    # Shared data components
│   │   ├── auth/                # TokenProvider interface, InMemoryTokenProvider
│   │   └── local/               # TokenStore (DataStore persistence)
│   ├── network/                 # AuthInterceptor
│   ├── ui/
│   │   ├── component/           # Reusable composables (headers, dropdowns)
│   │   ├── theme/               # Material3 theme, typography, dimensions
│   │   └── util/                # ClickGuard for preventing double-clicks
│   └── util/                    # Shared utilities
├── config/                      # ApiConfig (BASE_URL from BuildConfig)
├── di/                          # Hilt modules (DataModule, NetworkModule, DispatcherModule)
└── navigation/                  # AppNavGraph and Routes
```

### Feature Module Organization

Each feature module (`auth`, `stocklist`, `chart`) follows a consistent structure:

- **data/remote/**: Retrofit API interfaces and DTOs (data transfer objects) for network communication
- **data/repository/**: Repository classes that coordinate data sources and business logic
- **ui/**: Composable screens, UI state classes, and feature-specific UI components
  - May contain subdirectories for specific screens (e.g., `auth/ui/login/`)
  - May contain subdirectories for reusable UI components (e.g., `chart/ui/chart/`)
- **viewmodel/**: ViewModel classes with `@HiltViewModel` annotation

### Recommended Package Structure Guidelines

When adding new features or modifying existing ones, follow these conventions:

1. **API Interfaces & DTOs** → `feature/*/data/remote/`
   - Retrofit API interfaces
   - Request/Response DTOs used for API communication
   - Serializable data classes

2. **Repositories** → `feature/*/data/repository/`
   - Repository classes that orchestrate data operations
   - Business logic and data transformation
   - StateFlow/SharedFlow publishers

3. **UI State Classes** → `feature/*/ui/`
   - `*UiState` data classes that represent screen state
   - Place in the same package as their corresponding screen

4. **Composable Screens** → `feature/*/ui/`
   - Main screen composables
   - Screen-specific UI components
   - Organize into subdirectories by screen name if needed

5. **ViewModels** → `feature/*/viewmodel/`
   - ViewModel classes with `@HiltViewModel` and `@Inject constructor`

This structure provides:
- **Clear separation of concerns**: API/DTOs, business logic, and UI are clearly separated
- **Easy navigation**: All components for a feature are grouped together
- **Scalability**: New features follow the same pattern
- **Testability**: Dependencies are clear and easy to mock

## Key Implementation Notes

- **Hilt DI**: ViewModels use `@HiltViewModel` and `@Inject constructor` for dependency injection.
  Use `hiltViewModel()` in Compose screens to obtain ViewModel instances.
- **Coroutines**: All API calls use `viewModelScope.launch` with IO dispatcher (
  `withContext(Dispatchers.IO)`)
- **State Management**: Repositories use `MutableStateFlow` internally and expose `StateFlow`
  publicly for reactive updates
- **Error Handling**: ViewModels catch and map exceptions (HttpException → user-friendly messages,
  IOException → network errors, SerializationException → JSON errors)

## Creating Pull Requests

When creating pull requests, write clear and concise PR descriptions that include:

1. **Summary**: Brief explanation of what the PR does and why
2. **Changes**: Bulleted list of main changes
3. **Testing**: How the changes were tested (unit tests, integration tests, manual testing, etc.)
4. **Review Points** (optional): Specific areas or aspects that reviewers should focus o
