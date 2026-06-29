package tv.hdonlinetv.compose.ui.tv.onboarding

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.onboarding.OnboardingScreenBody

@OptIn(ExperimentalTvMaterial3Api::class)
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
