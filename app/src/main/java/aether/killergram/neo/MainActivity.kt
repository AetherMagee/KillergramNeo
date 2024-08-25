package aether.killergram.neo

import aether.killergram.neo.ui.tabs.FeaturesTab
import aether.killergram.neo.ui.tabs.SettingsTab
import aether.killergram.neo.ui.theme.KillergramNeoTheme
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KillergramNeoTheme {
                val items = listOf(
                    BottomNavigationItem(
                        title = "Features",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        route = "features"
                    ),
                    BottomNavigationItem(
                        title = "Settings",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        route = "settings"
                    )
                )

                var selectedItemIndex by remember { mutableIntStateOf(0) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = index == selectedItemIndex,
                                        onClick = {
                                            selectedItemIndex = index
                                        },
                                        label = {
                                            Text(text = item.title)
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.padding(it)) {
                            when (selectedItemIndex) {
                                0 -> FeaturesTab()
                                1 -> SettingsTab()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotEnabledWarning() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
            .background(Color.Red, shape = RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
        ) {
            Text(
                text = "The module is not enabled. Please enable it in LSPosed manager first and then restart this app.",
                style = TextStyle(color = Color.White, fontSize = 20.sp),
            )
        }
    }
}

@SuppressLint("WorldReadableFiles")
@Composable
fun RestartReminder() {
    ElevatedCard(
        onClick = { },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Tip"
            )
            Text(
                text = "Don't forget to restart your target app after making changes!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            )
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("WorldReadableFiles")
fun isLsposedAvailable(context: Context): Boolean {
    try {
        context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)
        return true
    } catch (e: SecurityException) {
        return false
    }
}

