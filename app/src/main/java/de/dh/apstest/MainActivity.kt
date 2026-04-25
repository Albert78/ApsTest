package de.dh.apstest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dh.apstest.data.GlucoseEntity
import de.dh.apstest.service.ApsService
import de.dh.apstest.ui.ApsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the background service
        startService(Intent(this, ApsService::class.java))

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApsDashboard()
                }
            }
        }
    }
}

@Composable
fun ApsDashboard(viewModel: ApsViewModel = viewModel()) {
    val readings by viewModel.glucoseReadings.collectAsState(initial = emptyList())

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
            items(readings) { reading ->
                GlucoseItem(reading)
            }
        }
    }
}

@Composable
fun GlucoseItem(reading: GlucoseEntity) {
    val sdf = SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale)
    val timeString = sdf.format(Date(reading.timestamp))

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
            Text(text = "${reading.value.toInt()} ${reading.unit}")
            Text(text = timeString)
        }
    }
}