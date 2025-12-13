# 📈 Stock View App (Kotlin / Jetpack Compose)

## 🧭 Overview

**A simple and lightweight stock chart application**
An intuitive app for viewing US stock candlestick charts with daily, weekly, and monthly intervals.
Built with Kotlin, Jetpack Compose, and MVVM architecture, featuring **login authentication** and **stock price display**.

<p align="center">
  <video src="https://github.com/user-attachments/assets/fd6fcc0c-c268-4e54-8930-1a236458c454" width="70%" controls></video>
</p>

---

## ⚙️ Key Features

### 🔐 Authentication

- Login with Email / Password
- JWT token-based authentication
- Token disposal on logout with navigation to login screen

### 💹 Stock Chart Display

- Display US stock candlesticks with daily, weekly, and monthly intervals
- Synchronized display of volume bars and charts
- Smooth animations when switching intervals

### ⚡ Asynchronous Processing

- Reactive data handling with Kotlin Coroutines + Flow
- Asynchronous API response fetching and error handling

### 🧪 Testing

- Unit tests for ViewModel / Repository layers
- Test environment with MockK + CoroutineTestRule

---

## 🛠️ Tech Stack

### 🧩 Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines / Flow
- **HTTP Communication**: Retrofit2 + OkHttp3
- **JSON Serialization**: Kotlinx Serialization
- **Chart Drawing**: MPAndroidChart
- **Authentication**: JWT (JSON Web Token)
- **Testing**: JUnit4 / MockK / CoroutineTestRule
- **DI**: Hilt
- **Build Management**: Gradle (KTS)
- **Version Control**: Git / GitHub

---

### 🧠 Additional Details

- **UI Design**: Uses Compose UI components based on Material Design 3
- **State Management**: Reactive UI updates with ViewModel + State + Flow
- **Data Layer**: Repository pattern separates API communication and caching
- **Design Philosophy**: MVVM-based, focusing on simple and testable structure
- **API Integration**: Communicates with Go (Gin) + Cloud Run backend
- **Environment Variables**: Switches API Base URL via BuildConfig (debug / release)

---

## 🏗️ Architecture

### 🧩 Design Philosophy

- **Architecture Pattern**: MVVM (Model - View - ViewModel)
- **Dependency Direction**: UI → ViewModel → Repository → DataSource
- **Purpose**: Separate UI logic and data processing for testability and maintainability

---

### 🧱 Layer Responsibilities

#### 🖥️ UI Layer (Jetpack Compose)

- Handles screen rendering and user input
- Automatically updates UI by observing `State` and `Flow`
- Notifies `ViewModel` of user operations as events

#### ⚙️ ViewModel Layer

- Manages app state and screen logic
- Fetches data from `Repository` and converts for UI display
- Maintains states like loading, success, failure as `UiState`

#### 🗂️ Repository Layer

- Unified intermediary layer for data acquisition
- Fetches data from `RemoteDataSource` (API communication)
- Performs DTO → UI Model conversion before returning to ViewModel

#### 🌐 DataSource Layer

- **RemoteDataSource**: API communication via `Retrofit` + `OkHttp`
  Attaches JWT token in headers to fetch stock data
- **LocalDataSource (Optional)**: Future expansion with Room for caching

---

### 🔁 Data Flow (Example: Stock Chart Display)

1. User selects interval (daily / weekly / monthly)
2. **UI → ViewModel**: Calls `viewModel.load(code, interval, outputsize)`
3. **ViewModel → Repository**: Executes `repo.fetchCandles(...)` in `viewModelScope.launch`
4. Repository fetches data via API and converts to UI data
5. ViewModel updates internal state
6. UI detects state change and redraws chart

---

### 🔓 Logout Flow

```
User taps logout button
       ↓
Screen (StockListScreen / ChartScreen)
       ↓ onLogout callback
NavGraph
       ↓ logoutViewModel.logout()
LogoutViewModel
       ↓ repo.logout() [IO dispatcher]
AuthRepository
       ├─ tokenProvider.clear()  ← Clear in-memory token
       └─ tokenStore.clear()     ← Clear DataStore token
       ↓
LogoutViewModel
       ↓ emit(UiEvent.LoggedOut)
NavGraph (LaunchedEffect)
       ↓ Collect event
Navigate to Login Screen (clear back stack)
```

**Separation of Concerns:**
- `LogoutViewModel`: Handles logout business logic
- `NavGraph`: Handles navigation only (listens for events and navigates)
- `AuthRepository`: Data layer (token management)

---

### 🚀 Future Improvements

- Local caching with Room
- Add UseCase layer for separation of concerns (Clean Architecture)

## 📂 Directory Structure

This project adopts a **feature-based** package structure.
By grouping related components (data, UI, ViewModel) by feature, modularity and maintainability are improved.

```text
app/
└── src/main/java/com/example/stock/
    ├── feature/                 # Feature modules
    │   ├── auth/                # Authentication feature
    │   │   ├── data/
    │   │   │   ├── remote/      # AuthApi (Retrofit), LoginRequest/Response DTOs
    │   │   │   └── repository/  # AuthRepository
    │   │   ├── ui/
    │   │   │   ├── login/       # LoginScreen, LoginUiState
    │   │   │   └── signup/      # SignupScreen, SignupUiState
    │   │   └── viewmodel/       # LoginViewModel, LogoutViewModel, SignupViewModel
    │   ├── stocklist/           # Stock list feature
    │   │   ├── data/
    │   │   │   ├── remote/      # StockApi (Retrofit), SymbolItem/CandleDto DTOs
    │   │   │   └── repository/  # StockRepository
    │   │   ├── ui/              # StockListScreen, SymbolUiState
    │   │   └── viewmodel/       # SymbolViewModel, SymbolViewModelFactory
    │   └── chart/               # Chart display feature
    │       ├── ui/              # ChartScreen, CandleUiState, MPAndroidChart views
    │       │   └── chart/       # CandleChartView, VolumeChartView, ChartSync
    │       └── viewmodel/       # CandlesViewModel, CandlesViewModelFactory
    ├── core/                    # Core components
    │   ├── data/                # Shared data components
    │   │   ├── auth/            # TokenProvider, InMemoryTokenProvider
    │   │   └── local/           # TokenStore (DataStore)
    │   ├── network/             # ApiClient, AuthInterceptor
    │   └── ui/                  # Common UI
    │       ├── component/       # Reusable Composables
    │       ├── theme/           # Material3 theme, typography
    │       └── util/            # Utilities like ClickGuard
    ├── config/                  # ApiConfig (BASE_URL configuration)
    ├── di/                      # Hilt modules (DataModule, NetworkModule, DispatcherModule)
    └── navigation/              # AppNavGraph, Routes
```

### 📦 Feature Module Structure

Each feature module (`auth`, `stocklist`, `chart`) consists of:

- **data/remote/** - Retrofit API interfaces and DTOs for network communication
- **data/repository/** - Repository classes that coordinate data operations
- **ui/** - Composable screens, UI state classes, feature-specific UI components
- **viewmodel/** - ViewModel and ViewModelFactory

This structure provides:

- Easy understanding of all components related to a specific feature
- Changes to one feature rarely affect others
- Easy separation into individual modules in the future

## ⚙️ Setup

### Prerequisites

- Android Studio Koala or later
- JDK 17 or higher
- Git installed

### Steps

```bash
# Clone
git clone https://github.com/UCHIDAnobuhiro/stock_frontend.git
cd stock_frontend

# Open in Android Studio and run (Build Variant: debug)
# Select build variant and start
```

### Build & Run Commands

```bash
# Build project
./gradlew build

# Build debug variant
./gradlew assembleDebug

# Install debug variant on connected device
./gradlew installDebug

# Build staging variant
./gradlew assembleStaging

# Build release variant
./gradlew assembleRelease
```

### Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.AuthViewModelTest"

# Run tests with coverage
./gradlew testDebugUnitTest --tests "*.AuthViewModelTest.*"

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean
```

### API Endpoint Configuration

- **Debug build**: `http://10.0.2.2:8080/` (Android emulator localhost)
- **Staging / Release build**: `https://api.stockviewapp.com/`

※ `BASE_URL` is retrieved from `BuildConfig.BASE_URL` and switches automatically per build variant.

## 🔄 CI / Test Automation

This repository uses **GitHub Actions** to automatically run tests when pull requests are created.
Unit tests for `ViewModel` and `Repository` are targeted,
and test results are automatically verified.

## 🚀 Future Work

### 💾 Caching with Room

- Introduce `Room` to cache candlestick data locally
- **Display recent charts offline**
- Integrate Remote / Local in `Repository` with automatic failover

### 🔐 Server-side JWT Management (Hybrid Approach)

- Adopt **short-lived JWT access tokens (5-10 minutes)**
- Issue **server-managed opaque refresh tokens** per device with rotation (/auth/refresh)
- Support immediate invalidation per device via `/auth/logout`
- Store securely on client with EncryptedSharedPreferences + Keystore

### ⚙️ Improvement Ideas

- Add UseCase layer (separate screen logic from business logic)
- Extend theme switching (Light/Dark)
