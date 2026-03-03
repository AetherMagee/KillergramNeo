package aether.killergram.neo

import aether.killergram.neo.ui.screens.MainScreen
import aether.killergram.neo.ui.theme.KillergramNeoTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KillergramNeoTheme {
                MainScreen()
            }
        }
    }
}
