package com.ioristudios.ac

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ioristudios.ac.data.AcStateRepository
import com.ioristudios.ac.hardware.AndroidConsumerIrDriver
import com.ioristudios.ac.hardware.IrTransmissionService
import com.ioristudios.ac.ui.screens.SmartRemoteScreen
import com.ioristudios.ac.ui.theme.IoriAcTheme
import com.ioristudios.ac.ui.theme.OledBlack
import com.ioristudios.ac.viewmodel.RemoteViewModel
import com.ioristudios.ac.viewmodel.RemoteViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels {
        val driver = AndroidConsumerIrDriver(applicationContext)
        val transmissionService = IrTransmissionService(driver)
        val repository = AcStateRepository(applicationContext)
        RemoteViewModelFactory(repository, transmissionService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            IoriAcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OledBlack
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    SmartRemoteScreen(viewModel = viewModel, uiState = uiState)
                }
            }
        }
    }
}
