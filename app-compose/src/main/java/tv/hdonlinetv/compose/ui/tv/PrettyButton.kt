package tv.hdonlinetv.compose.ui.tv

import androidx.compose.runtime.Composable
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PrettyButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = label)
    }
}
