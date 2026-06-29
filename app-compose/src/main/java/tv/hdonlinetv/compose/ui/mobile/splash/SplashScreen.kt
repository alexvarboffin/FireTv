package tv.hdonlinetv.compose.ui.mobile.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import androidx.compose.foundation.Image

@Composable
fun SplashScreen() {
    val navController = LocalPhoneNavController.current
    val settingsRepository = LocalSettingsRepository.current
    LaunchedEffect(Unit) {
        delay(800)
        val destination = if (settingsRepository.getSettings().isFirstLaunch) {
            Routes.Onboarding.route
        } else {
            Routes.Main.route
        }
        navController.navigate(destination) {
            popUpTo(Routes.Splash.route) { inclusive = true }
        }
    }
    SplashScreenBody()
}

@Composable
fun SplashScreenBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bgMain)),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(90.dp),
            )
            Text(
                text = stringResource(R.string.app_name_legacy),
                color = colorResource(R.color.black),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.splash_subtitle),
                color = colorResource(R.color.black),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(100.dp),
            color = colorResource(R.color.colorAccent),
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            color = colorResource(R.color.tabText),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )
    }
}
