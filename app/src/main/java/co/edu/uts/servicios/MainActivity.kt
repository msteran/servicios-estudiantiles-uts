package co.edu.uts.servicios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uts.servicios.data.LocalStudentRepository
import co.edu.uts.servicios.ui.PortalApp
import co.edu.uts.servicios.ui.UtsTheme
import co.edu.uts.servicios.viewmodel.PortalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = PortalViewModel.Factory(LocalStudentRepository(applicationContext))
        setContent {
            UtsTheme { PortalApp(viewModel(factory = factory)) }
        }
    }
}
