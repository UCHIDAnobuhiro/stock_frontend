# Stock List Feature

## Summary

The `stocklist` feature module displays a list of stock symbols fetched from the API. It follows the
MVVM architecture pattern with clear separation between data, UI, and ViewModel layers.

### Key Features

- **Symbol List Display**: Shows a list of stock symbols with name and code
- **Loading State**: Displays progress indicator while fetching data
- **Error Handling**: Shows error message with retry button on failure
- **Empty State**: Displays message when no symbols are available
- **Navigation**: Tapping a symbol navigates to the chart screen

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                     SymbolListScreen                            │    │
│  │  ┌─────────────────────────────────────────────────────────┐    │    │
│  │  │              SymbolListScreenContent                    │    │    │
│  │  │  ┌───────────────┐                                      │    │    │
│  │  │  │ SymbolUiState │                                      │    │    │
│  │  │  └───────────────┘                                      │    │    │
│  │  └─────────────────────────────────────────────────────────┘    │    │
│  └──────────────────────────────┬──────────────────────────────────┘    │
└─────────────────────────────────┼───────────────────────────────────────┘
                                  │ observes
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel Layer                               │
│                    ┌─────────────────────┐                              │
│                    │   SymbolViewModel   │                              │
│                    │  - ui: StateFlow    │                              │
│                    │  - load()           │                              │
│                    └──────────┬──────────┘                              │
└───────────────────────────────┼─────────────────────────────────────────┘
                                │ calls
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository Layer                               │
│                    ┌─────────────────────┐                              │
│                    │  SymbolRepository   │                              │
│                    │  - fetchSymbols()   │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
                               ▼
                    ┌─────────────────────┐
                    │     SymbolApi       │
                    │    (Retrofit)       │
                    │  - getSymbols()     │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │    Backend API      │
                    │   GET /symbols      │
                    └─────────────────────┘
```

## Directory Structure

```
feature/stocklist/
├── data/
│   ├── remote/
│   │   ├── SymbolApi.kt        # Retrofit interface for symbol endpoints
│   │   └── SymbolDto.kt        # DTO for symbol data
│   └── repository/
│       └── SymbolRepository.kt # Fetches symbol list from API
│
├── ui/
│   ├── SymbolListScreen.kt     # Symbol list composable with Hilt ViewModel injection
│   └── SymbolUiState.kt        # Symbol list screen state data class
│
├── viewmodel/
│   └── SymbolViewModel.kt      # Manages symbol list state and business logic
│
└── README.md                   # This file
```

## Testing

### Test Location

Tests are located at:

| Test Type  | Location                                           |
|------------|----------------------------------------------------|
| Unit Tests | `app/src/test/java/com/example/stock/feature/stocklist/` |

### Test Files

#### Unit Tests

| File                                        | Description                                          |
|---------------------------------------------|------------------------------------------------------|
| `viewmodel/SymbolViewModelTest.kt`          | Tests for SymbolViewModel including load success/failure |
| `data/repository/SymbolRepositoryTest.kt`   | Tests for SymbolRepository API calls                 |

### Running Tests

```bash
# Run all unit tests for stocklist feature
./gradlew testDebugUnitTest --tests "*.SymbolViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.SymbolRepositoryTest.*"
```

### Test Coverage

#### SymbolViewModel Tests

- Load success - updates symbols and clears error
- Load failure - sets error and keeps symbols unchanged
- Load clears previous error on new request start

#### SymbolRepository Tests

- fetchSymbols returns symbols from API

### Testing Patterns

The feature uses the following testing patterns:

1. **MainDispatcherRule**: Replaces `Dispatchers.Main` with `StandardTestDispatcher` for
   deterministic coroutine testing
2. **MockK**: Mocking framework with `relaxed = true` for simple mocks
3. **Truth**: Assertion library using `assertThat()` style

Example test pattern:

```kotlin
@Test
fun `load success - updates symbols and clears error`() = runTest(mainRule.scheduler) {
    // given
    val expected = listOf(
        SymbolDto("AAPL", "Apple Inc."),
        SymbolDto("GOOG", "Alphabet Inc.")
    )
    coEvery { repo.fetchSymbols() } returns expected

    // when
    vm.load()
    advanceUntilIdle()

    // then
    val state = vm.ui.value
    assertThat(state.isLoading).isFalse()
    assertThat(state.error).isNull()
    assertThat(state.symbols).isEqualTo(expected)
}
```
