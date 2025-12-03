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
./gradlew testDebugUnitTest --tests "com.example.stock.viewmodel.AuthViewModelTest"

# Run tests with coverage
./gradlew testDebugUnitTest --tests "*.AuthViewModelTest.*"

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

**Factory Pattern**: ViewModels use custom factories (`AuthViewModelFactory`,
`SymbolViewModelFactory`, `CandlesViewModelFactory`) for dependency injection since Hilt/Koin is not
yet integrated.

**Token Management**: Dual-layer approach with `InMemoryTokenProvider` (in-memory) + `TokenStore` (
DataStore persistence). `AuthInterceptor` automatically adds JWT Bearer token to API requests.

### Navigation Flow

1. `LoginScreen` (Routes.LOGIN) → validates email/password → calls `AuthViewModel.login()`
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
│   │   ├── data/                # AuthRepository, AuthApi, LoginRequest/Response DTOs
│   │   ├── ui/                  # LoginScreen, LoginUiState
│   │   └── viewmodel/           # AuthViewModel, AuthViewModelFactory
│   ├── stocklist/               # Stock list feature
│   │   ├── data/                # StockRepository (symbols), StockApi, SymbolItem DTO
│   │   ├── ui/                  # StockListScreen, SymbolUiState
│   │   └── viewmodel/           # SymbolViewModel, SymbolViewModelFactory
│   └── chart/                   # Chart display feature
│       ├── data/                # CandlesRepository, CandleDto, CandleUiState
│       ├── ui/                  # ChartScreen, MPAndroidChart views, ChartSync
│       └── viewmodel/           # CandlesViewModel, CandlesViewModelFactory
├── core/
│   ├── data/                    # Shared data components
│   │   ├── auth/                # TokenProvider interface, InMemoryTokenProvider
│   │   └── local/               # TokenStore (DataStore persistence)
│   ├── network/                 # ApiClient singleton, AuthInterceptor
│   ├── ui/
│   │   ├── component/           # Reusable composables (headers, dropdowns)
│   │   ├── theme/               # Material3 theme, typography, dimensions
│   │   └── util/                # ClickGuard for preventing double-clicks
│   └── util/                    # Shared utilities
├── config/                      # ApiConfig (BASE_URL from BuildConfig)
└── navigation/                  # AppNavGraph and Routes
```

### Feature Module Organization

Each feature module (`auth`, `stocklist`, `chart`) is self-contained and includes:
- **data/**: Repository, API interface, DTOs, and domain models specific to the feature
- **ui/**: Composable screens, UI state classes, and feature-specific UI components
- **viewmodel/**: ViewModel and ViewModelFactory for the feature

This structure makes it easy to:
- Understand all components related to a specific feature
- Modify or extend a feature without affecting others
- Potentially extract features into separate modules in the future

## Key Implementation Notes

- **Manual DI**: Uses ViewModelFactory pattern. All ViewModels are instantiated in `MainActivity`
  via `by viewModels<T> { Factory(...) }` and passed to composables.
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
