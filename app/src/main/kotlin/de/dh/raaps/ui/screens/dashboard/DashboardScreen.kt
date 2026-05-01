package de.dh.raaps.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dh.eventseries.ui.composables.WarningBanner
import de.dh.eventseries.ui.composables.screenTitle
import de.dh.raaps.R
import de.dh.raaps.ui.screens.common.BgHistoryChart
import de.dh.raaps.ui.screens.permissions.PermissionStatus
import de.dh.raaps.ui.screens.permissions.PermissionsUiModel
import de.dh.raaps.ui.screens.permissions.PermissionsViewModel
import de.dh.raaps.ui.theme.ApsTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    historyViewModel:HistoryViewModel,
    permissionsViewModel: PermissionsViewModel,
    onFixPermissions: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPreferences: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyUiState by historyViewModel.uiState.collectAsState()
    val permissionsUiState by permissionsViewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        historyUiState = historyUiState,
        permissionsUiState = permissionsUiState,
        onFixPermissionsClick = onFixPermissions,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToPreferences = onNavigateToPreferences
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    historyUiState: HistoryUiState,
    permissionsUiState: PermissionsUiModel,
    onFixPermissionsClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPreferences: () -> Unit
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
                    // Permissions warning header
                    if (!permissionsUiState.isPermissionsConfigComplete) {
                        WarningBanner(
                            warningText = stringResource(id = R.string.dashboard_permissions_missing),
                            actionText = stringResource(id = R.string.dashboard_fix_permissions_link),
                            onActionClick = onFixPermissionsClick
                        )
                    }

                    Text(
                        text = stringResource(R.string.dashboard_glucose_title),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    BgHistoryChart(
                    )
                }
            }
        }
    }
}

fun createSampleHistoryUiState(): HistoryUiState {
    return HistoryUiState(isLoading = false, isError = false)
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    ApsTheme {
        DashboardContent(
            uiState = DashboardUiState(isLoading = false, isError = false),
            historyUiState = createSampleHistoryUiState(),
            permissionsUiState = PermissionsUiModel(
                isLoading = false,
                notificationPermissionStatus = PermissionStatus.Granted,
                ignoreBatteryOptimizationPermissionStatus = PermissionStatus.Granted,
                autoRevokePermissionsPermissionStatus = PermissionStatus.Granted,
                numPermissionsMissing = 0,
                permissionsMissingText = ""
            ),
            onFixPermissionsClick = {},
            onNavigateToPermissions = {},
            onNavigateToPreferences = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPermissionsWarningPreview() {
    ApsTheme {
        DashboardContent(
            uiState = DashboardUiState(isLoading = false, isError = false),
            historyUiState = createSampleHistoryUiState(),
            permissionsUiState = PermissionsUiModel(
                isLoading = false,
                notificationPermissionStatus = PermissionStatus.Denied,
                ignoreBatteryOptimizationPermissionStatus = PermissionStatus.Granted,
                autoRevokePermissionsPermissionStatus = PermissionStatus.Granted,
                numPermissionsMissing = 1,
                permissionsMissingText = "1 permission missing"
            ),
            onFixPermissionsClick = {},
            onNavigateToPermissions = {},
            onNavigateToPreferences = {},
        )
    }
}