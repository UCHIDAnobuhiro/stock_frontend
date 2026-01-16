package com.example.stock.feature.stocklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.component.MainHeader
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.core.ui.theme.Thickness
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel

/**
 * Symbol list screen with ViewModel.
 *
 * Wrapper composable that connects [SymbolViewModel] to [SymbolListScreenContent].
 * Handles state observation and initial data loading.
 * Uses Hilt to automatically inject the SymbolViewModel.
 *
 * @param onNavigateToChart Callback to navigate to chart screen with symbol name and code
 * @param onLogout Callback invoked when logout button is pressed
 * @param viewModel Symbol list ViewModel (injected by Hilt)
 */
@Composable
fun SymbolListScreen(
    onNavigateToChart: (name: String, code: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: SymbolViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    // Load symbol list on first display
    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    SymbolListScreenContent(
        uiState = uiState,
        onSymbolClick = onNavigateToChart,
        onReload = viewModel::load,
        onLogout = onLogout
    )
}

/**
 * Stateless symbol list screen content.
 *
 * Displays a list of symbols fetched from the API.
 * Shows loading indicator, error state with retry button, or empty state as appropriate.
 *
 * @param uiState Current UI state
 * @param onSymbolClick Callback when a symbol item is clicked
 * @param onReload Callback to reload the symbol list
 * @param onLogout Callback when logout button is pressed
 */
@Composable
fun SymbolListScreenContent(
    uiState: SymbolUiState,
    onSymbolClick: (name: String, code: String) -> Unit,
    onReload: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            MainHeader(
                titleText = stringResource(R.string.app_header_stock_list),
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(Spacing.Screen)

        when {
            // Loading state
            uiState.isLoading -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error state
            uiState.errorResId != null -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(uiState.errorResId), color = MaterialTheme.colorScheme.error)
                        Spacer(
                            modifier = Modifier.padding(top = Spacing.ListItemVertical)
                        )
                        Button(onClick = onReload) {
                            Text(stringResource(R.string.reload))
                        }
                    }
                }
            }

            // Empty state
            uiState.symbols.isEmpty() -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_symbols))
                }
            }

            // Symbol list display
            else -> {
                Column(modifier) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            uiState.symbols,
                            key = { _, item -> item.code }
                        ) { index, symbol ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Navigate to chart screen on tap
                                    .clickable {
                                        onSymbolClick(symbol.name, symbol.code)
                                    }
                                    .background(Color.Transparent)
                                    .padding(vertical = Spacing.ListItemVertical),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Symbol name
                                Text(text = symbol.name, style = MaterialTheme.typography.bodyLarge)
                                // Symbol code
                                Text(text = symbol.code, style = MaterialTheme.typography.bodyLarge)
                            }
                            if (index < uiState.symbols.lastIndex) {
                                HorizontalDivider(thickness = Thickness.Divider)
                            }
                        }
                    }
                }
            }
        }
    }
}
