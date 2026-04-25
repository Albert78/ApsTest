package de.dh.apstest.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.dh.apstest.MainApplication
import de.dh.apstest.data.GlucoseEntity
import kotlinx.coroutines.flow.Flow

class ApsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as MainApplication).database
    
    val glucoseReadings: Flow<List<GlucoseEntity>> = database.glucoseDao().getAllReadings()
}