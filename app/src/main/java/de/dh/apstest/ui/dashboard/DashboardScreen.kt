package de.dh.apstest.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.dh.apstest.ui.common.shortDateTime
import de.dh.apstest.ui.permissions.PermissionsUiModel
import de.dh.apstest.ui.permissions.PermissionsViewModel
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "AAPS Core Blueprint",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

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
//            items(readings) { reading ->
//                GlucoseItem(reading)
//            }
            item {
                GlucoseItem(125.0, LocalDateTime.now())
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