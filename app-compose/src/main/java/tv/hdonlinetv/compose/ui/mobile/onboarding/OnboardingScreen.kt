package tv.hdonlinetv.compose.ui.mobile.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import androidx.compose.foundation.Image

@Composable
fun OnboardingScreen() {
    val navController = LocalPhoneNavController.current
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

@Composable
fun OnboardingScreenBody(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorPrimary))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_title),
            color = colorResource(R.color.white),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            color = colorResource(R.color.white),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(
            onClick = onGetStarted,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white),
                contentColor = colorResource(R.color.colorPrimary),
            ),
        ) {
            Text(stringResource(R.string.get_started))
        }
    }
}
