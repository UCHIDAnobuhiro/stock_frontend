# Chart Feature

## Summary

The `chart` feature module displays candlestick charts for stock price data using MPAndroidChart
library. It follows the MVVM architecture pattern with clear separation between data, UI, and
ViewModel layers.

### Key Features

- **Candlestick Chart**: Displays OHLC (Open, High, Low, Close) data with color-coded candles
- **Volume Chart**: Displays trading volume as bar chart below the candlestick chart
- **Chart Synchronization**: Bidirectional sync for zoom, scroll, and highlight between charts
- **Interval Selection**: Supports daily, weekly, and monthly data intervals
- **Loading State**: Shows loading indicator while fetching data
- **Error Handling**: Displays localized error messages using string resources

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                       ChartScreen                               │    │
│  │  ┌───────────────┐  ┌─────────────────┐  ┌─────────────────┐    │    │
│  │  │ CandleUiState │  │ CandleChartView │  │ VolumeChartView │    │    │
│  │  └───────────────┘  └────────┬────────┘  └────────┬────────┘    │    │
│  │                              │                    │              │    │
│  │                              └────────┬───────────┘              │    │
│  │                                       │                          │    │
│  │                              ┌────────▼────────┐                 │    │
│  │                              │   ChartSync     │                 │    │
│  │                              │ (Synchronizes   │                 │    │
│  │                              │  zoom/scroll)   │                 │    │
│  │                              └─────────────────┘                 │    │
│  └──────────────────────────────┬──────────────────────────────────┘    │
└─────────────────────────────────┼───────────────────────────────────────┘
                                  │ observes
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel Layer                               │
│                    ┌─────────────────────┐                              │
│                    │  CandlesViewModel   │                              │
│                    │  - ui: StateFlow    │                              │
│                    │  - load()           │                              │
│                    │  - clear()          │                              │
│                    └──────────┬──────────┘                              │
└───────────────────────────────┼─────────────────────────────────────────┘
                                │ calls
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository Layer                               │
│                    ┌─────────────────────┐                              │
│                    │  CandleRepository   │                              │
│                    │  - candles: Flow    │                              │
│                    │  - fetchCandles()   │                              │
│                    │  - clearCandles()   │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
                               ▼
                    ┌─────────────────────┐
                    │      ChartApi       │
                    │     (Retrofit)      │
                    │  - getCandles()     │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │    Backend API      │
                    │ GET /candles/{code} │
                    └─────────────────────┘
```

## Directory Structure

```
feature/chart/
├── data/
│   ├── remote/
│   │   └── ChartApi.kt          # Retrofit interface + CandleDto
│   └── repository/
│       └── CandleRepository.kt  # Fetches and caches candle data
│
├── ui/
│   ├── CandleItem.kt            # UI model for candle data
│   ├── CandleUiState.kt         # Chart screen state data class
│   ├── ChartScreen.kt           # Main chart composable
│   └── chart/                   # Chart components subdirectory
│       ├── CandleChartView.kt   # Candlestick chart (MPAndroidChart)
│       ├── VolumeChartView.kt   # Volume bar chart (MPAndroidChart)
│       ├── ChartSync.kt         # Bidirectional sync logic
│       ├── SyncChartsOnce.kt    # Composable wrapper for sync setup
│       ├── ChartStyle.kt        # Chart styling and theming
│       ├── ChartToken.kt        # Design tokens and constants
│       └── ChartUtils.kt        # Axis formatting utilities
│
├── viewmodel/
│   └── CandlesViewModel.kt      # Manages chart state and data loading
│
└── README.md                    # This file
```

## Chart Synchronization

The chart feature implements sophisticated bidirectional synchronization between candlestick and
volume charts:

### How It Works

1. **AtomicBoolean Locks**: `vpLock` and `hlLock` prevent infinite callback loops
2. **Viewport Sync**: When one chart is zoomed/scrolled, the other follows
3. **Highlight Sync**: Selecting a candle highlights the corresponding volume bar
4. **Disabled Inertia**: `isDragDecelerationEnabled = false` prevents scroll lag

### Key Components

| Component         | Purpose                                          |
|-------------------|--------------------------------------------------|
| `ChartSync.kt`    | Contains `attachSynchronizedPair()` function     |
| `SyncChartsOnce`  | Composable ensuring one-time sync attachment     |
| `ChartStyle.kt`   | Applies consistent styling to both charts        |

## Testing

### Test Location

Tests are located at:

| Test Type  | Location                                              |
|------------|-------------------------------------------------------|
| Unit Tests | `app/src/test/java/com/example/stock/feature/chart/`  |

### Test Files

#### Unit Tests

| File                                        | Description                                            |
|---------------------------------------------|--------------------------------------------------------|
| `viewmodel/CandlesViewModelTest.kt`         | Tests for CandlesViewModel load/clear/error handling   |
| `data/repository/CandleRepositoryTest.kt`   | Tests for CandleRepository API calls                   |

### Running Tests

```bash
# Run all unit tests for chart feature
./gradlew testDebugUnitTest --tests "*.CandlesViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.CandleRepositoryTest.*"
```

### Test Coverage

#### CandlesViewModel Tests

- Load success - transforms DTOs to UI items and clears loading
- Load failure (IOException) - sets network error resource ID
- Load with blank code - sets validation error without calling repository
- Load calls fetchCandles with correct parameters
- Clear resets UI state and cancels in-flight requests

#### CandleRepository Tests

- fetchCandles stores data in StateFlow
- clearCandles resets the flow to empty list

### Testing Patterns

The feature uses the following testing patterns:

1. **MainDispatcherRule**: Replaces `Dispatchers.Main` with `StandardTestDispatcher`
2. **DispatcherProvider Mock**: Injects test dispatchers for deterministic testing
3. **MockK**: Mocking framework with `relaxed = true` for simple mocks
4. **Truth**: Assertion library using `assertThat()` style

Example test pattern:

```kotlin
@Test
fun `load sets error when repository throws IOException`() =
    runTest(mainRule.scheduler) {
        // given
        coEvery { repo.fetchCandles(any(), any(), any()) } throws IOException("network")

        // when
        vm.load("AAPL")
        advanceUntilIdle()

        // then
        val ui = vm.ui.value
        assertThat(ui.isLoading).isFalse()
        assertThat(ui.errorResId).isEqualTo(R.string.error_network)
        assertThat(ui.items).isEmpty()
    }
```
