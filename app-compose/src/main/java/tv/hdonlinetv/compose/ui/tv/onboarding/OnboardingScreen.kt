package tv.hdonlinetv.compose.ui.tv.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun OnboardingScreen() {
    val navController = LocalTvNavController.current
    val settingsRepository = LocalSettingsRepository.current
    OnboardingScreenBody(
        onGetStarted = {
            settingsRepository.completeFirstLaunch()
            navController.navigate(Routes.Main.route) {
                popUpTo(Routes.Onboarding.route) { inclusive = true }
            }
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OnboardingScreenBody(onGetStarted: () -> Unit) {
    val startFocus = remember { FocusRequester() }
    val primary = colorResource(R.color.colorPrimary)
    val white = colorResource(R.color.white)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primary)
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_title),
            color = white,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            color = white,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 720.dp)
                .padding(vertical = 16.dp),
        )
        Button(
            onClick = onGetStarted,
            colors = ButtonDefaults.colors(
                containerColor = white,
                contentColor = primary,
                focusedContainerColor = white,
                focusedContentColor = primary,
            ),
            modifier = Modifier.focusRequester(startFocus),
        ) {
            Text(text = stringResource(R.string.get_started))
        }
    }
    LaunchedEffect(Unit) {
        runCatching { startFocus.requestFocus() }
    }
}
