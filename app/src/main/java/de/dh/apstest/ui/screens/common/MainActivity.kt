package de.dh.apstest.ui.screens.common

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.dh.apstest.MainApplication
import de.dh.apstest.service.ApsService
import de.dh.apstest.ui.screens.dashboard.DashboardScreen
import de.dh.apstest.ui.screens.dashboard.DashboardViewModel
import de.dh.apstest.ui.screens.permissions.PermissionsViewModel
import de.dh.apstest.ui.theme.ApsTheme
import kotlinx.serialization.Serializable

// --- Navigation Routes

@Serializable object ApsDashboardRoute : NavKey

@Serializable object Permissions : NavKey

@Serializable object PreferencesMain : NavKey

@Serializable object DataManagement : NavKey

class MainActivity : ComponentActivity() {
    private lateinit var navViewModel: NavigationViewModel
    private lateinit var permissionsViewModel: PermissionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the background service
        startForegroundService(Intent(this, ApsService::class.java))

        navViewModel = ViewModelProvider(
            this,
            NavigationViewModel.Companion.NavigationViewModelFactory(listOf(ApsDashboardRoute))
        )[NavigationViewModel::class.java]

        val application = application as MainApplication

        permissionsViewModel = ViewModelProvider(
            this,
            PermissionsViewModel.Companion.Factory(application)
        )[PermissionsViewModel::class.java]

        setContent {
            ApsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }

    @Composable
    fun MainApp() {
        val application = application as MainApplication

        val backStack by navViewModel.backstack.collectAsState()

        NavDisplay(
            backStack = backStack,
            onBack = { navViewModel.pop() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<ApsDashboardRoute> { _ ->
                    val vm: DashboardViewModel =
                        viewModel(factory = DashboardViewModel.Companion.Factory(application))
                    val permissionsViewModel: PermissionsViewModel =
                        viewModel(factory = PermissionsViewModel.Companion.Factory(application))

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        vm.reload()
                        permissionsViewModel.updateAppPermissions()
                    }

                    DashboardScreen(
                        viewModel = vm,
                        permissionsViewModel = permissionsViewModel,
                        onFixPermissions = { navViewModel.push(Permissions) },
                        onNavigateToPermissions = { navViewModel.push(Permissions) },
                        onNavigateToPreferences = { navViewModel.push(PreferencesMain) },
                        onNavigateToDataManagement = { navViewModel.push(DataManagement) }
                    )
                }
            }
        )
    }
}