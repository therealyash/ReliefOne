package com.initbase.reliefone.features.sos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.initbase.reliefone.R
import com.initbase.reliefone.core.data.source.contactsDataStore
import com.initbase.reliefone.features.sos.presentation.send_sos.SendSosScreen
import com.initbase.reliefone.features.sos.presentation.send_sos.SosViewModel
import com.initbase.reliefone.features.sos.presentation.send_sos.SosViewModelFactory
import com.initbase.reliefone.ui.theme.SOSAppTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val dataStore by lazy { applicationContext.contactsDataStore }
    private val viewModel by viewModels<SosViewModel>{SosViewModelFactory(dataStore,this)}

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SOSAppTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    SendSosScreen(viewModel,cameraExecutor)
                }
            }
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}