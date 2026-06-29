package tv.hdonlinetv.compose.ui.mobile

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PrettyButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = label)
    }
}
