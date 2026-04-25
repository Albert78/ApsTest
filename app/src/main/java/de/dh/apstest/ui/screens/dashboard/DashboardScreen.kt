package de.dh.apstest.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dh.apstest.R
import de.dh.apstest.ui.screens.common.shortDateTime
import de.dh.apstest.ui.screens.permissions.PermissionsUiModel
import de.dh.apstest.ui.screens.permissions.PermissionsViewModel
import de.dh.eventseries.ui.composables.screenTitle
import java.time.LocalDateTime

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    permissionsViewModel: PermissionsViewModel,
    onFixPermissions: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToDataManagement: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionsUiState by permissionsViewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        permissionsUiState = permissionsUiState,
        onFixPermissionsClick = onFixPermissions,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToPreferences = onNavigateToPreferences,
        onNavigateToDataManagement = onNavigateToDataManagement
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    permissionsUiState: PermissionsUiModel,
    onFixPermissionsClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToDataManagement: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = screenTitle(stringResource(id = R.string.aps_dashboard_screen_title)),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.cd_more_options))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_item_permissions_label)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToPermissions()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_item_preferences_label)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToPreferences()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_item_datamanagement_label)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToDataManagement()
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }, floatingActionButton = {
//            if (!uiState.isLoading && !uiState.isError) {
//                ExtendedFloatingActionButton(
//                    onClick = onAddEventSeries,
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(Modifier.width(8.dp))
//                    Text(text = stringResource(R.string.add_button))
//                }
//            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Status: Running",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Recent Glucose Readings:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
//                        items(readings) { reading ->
//                            GlucoseItem(reading)
//                        }
                        item {
                            GlucoseItem(125.0, LocalDateTime.now())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlucoseItem(value: Double, time: LocalDateTime) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${value} mg/dl")
            Text(text = shortDateTime(time))
        }
    }
}