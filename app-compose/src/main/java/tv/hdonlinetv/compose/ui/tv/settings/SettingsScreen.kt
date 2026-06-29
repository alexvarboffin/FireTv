package tv.hdonlinetv.compose.ui.tv.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun SettingsScreen() {
    val navController = LocalTvNavController.current
    SettingsScreenBody(onBack = { navController.popBackStack() })
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreenBody(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.menu_settings))
    }
}
