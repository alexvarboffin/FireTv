package tv.hdonlinetv.compose.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button as MdButton
import androidx.compose.material3.ButtonDefaults as MdButtonDefaults
import androidx.compose.material3.Icon as MdIcon
import androidx.compose.material3.Text as MdText
import androidx.tv.material3.Border
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.ButtonDefaults as TvButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text as TvText
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.ui.mobile.theme.PhoneTheme
import tv.hdonlinetv.compose.ui.tv.theme.TvTheme

/**
 * Stateless Compose dialog that mirrors the legacy RateMeDialog shown by
 * [com.walhalla.ui.observer.RateAppModule] (`com.androidsx.rateme.RateMeDialog`,
 * layouts `rateme__dialog_title` / `rateme__dialog_message`).
 *
 * This composable is UI-only: no ViewModel, no Play/RuStore review API calls,
 * no preferences and no DI. It does not depend on the R6a `:ratemyapp` module;
 * the host maps these callbacks after R6a is published:
 * - [onRate] → `onRated(stars)` then `RateUiState.OpenStore` (positive threshold = 5)
 * - [onLater] → `onLater()` after 1–4 stars; host should then show [RateThankYouDialog]
 *   (upcoming `RateUiState.ThankYou`, then email to the developer)
 * - [onNever] → `onNever()`
 * - [onDismiss] for Back (legacy used `setCancelable(false)`; TV/D-pad still needs Back)
 *
 * R6a will set `showOnTv = false`, so this dialog is not presented on TV in production.
 * D-pad focus, focus indication and TV previews are still implemented here for completeness.
 *
 * Legacy action mapping:
 * - 5 stars + "Rate" (`rateme__dialog_first_button_rate`) → [onRate]
 * - 1–4 stars + "Thanks" (`rateme__dialog_first_thanks`) → [onLater]
 * - Close (X) → [onNever] (legacy: `appRated(false)`, opt-out, DISMISSED_WITH_CROSS)
 * - Back → [onDismiss]
 * - Optional share icon (hidden by default, same as the RateAppModule builder) → [onShare]
 *
 * @param visible whether the dialog is shown
 * @param appName app display name interpolated into `rateme__dialog_first_message`
 * @param selectedStars currently selected integer rating, 0..5 (legacy step size is 1)
 * @param showShareButton when true, shows the title-bar share icon
 * @param showAppIcon when true and [appIcon] is set, shows the 88.dp icon (off by default)
 * @param appIcon optional icon painter used only when [showAppIcon] is true
 * @param onStarsChange called when the user picks a star; keep this controlled/stateless
 * @param onRate 5-star confirm ("Rate our app…")
 * @param onLater 1–4 star confirm ("Thanks for the rating!")
 * @param onNever close (X)
 * @param onDismiss Back / system dismiss request
 * @param onShare title-bar share, only if [showShareButton] is true
 */
@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RateAppDialog(
    visible: Boolean,
    appName: String,
    selectedStars: Int,
    onStarsChange: (Int) -> Unit,
    onRate: (stars: Int) -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showShareButton: Boolean = false,
    showAppIcon: Boolean = false,
    appIcon: Painter? = null,
    onShare: () -> Unit = {},
) {
    if (!visible) return

    val isTv = isTelevision()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        RateAppDialogContent(
            appName = appName,
            selectedStars = selectedStars.coerceIn(0, 5),
            onStarsChange = onStarsChange,
            onRate = onRate,
            onLater = onLater,
            onNever = onNever,
            onShare = onShare,
            showShareButton = showShareButton,
            showAppIcon = showAppIcon,
            appIcon = appIcon,
            isTv = isTv,
            modifier = modifier,
        )
    }
}

/** Visual body of [RateAppDialog], extracted so previews can render it without a window. */
@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RateAppDialogContent(
    appName: String,
    selectedStars: Int,
    onStarsChange: (Int) -> Unit,
    onRate: (stars: Int) -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
    modifier: Modifier = Modifier,
    showShareButton: Boolean = false,
    showAppIcon: Boolean = false,
    appIcon: Painter? = null,
    onShare: () -> Unit = {},
    isTv: Boolean = false,
) {
    val stars = selectedStars.coerceIn(0, 5)
    val showRate = stars >= 5
    val showThanks = stars in 1..4
    val firstStarFocus = remember { FocusRequester() }
    val primaryActionFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (showRate || showThanks) {
            primaryActionFocus.requestFocus()
        } else {
            firstStarFocus.requestFocus()
        }
    }
    LaunchedEffect(showRate, showThanks) {
        if (showRate || showThanks) {
            primaryActionFocus.requestFocus()
        }
    }

    val maxWidth = if (isTv) 560.dp else 400.dp
    Column(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth()
            .focusGroup()
            .focusRestorer(firstStarFocus)
            .background(RateAppDialogDefaults.BodyBackground)
            .border(1.dp, RateAppDialogDefaults.HeaderBackground),
    ) {
        RateAppDialogTitleBar(
            showShareButton = showShareButton,
            onShare = onShare,
            onNever = onNever,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(RateAppDialogDefaults.BodyBackground)
                .padding(RateAppDialogDefaults.DefaultPadding),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                if (showAppIcon && appIcon != null) {
                    Image(
                        painter = appIcon,
                        contentDescription = stringResource(R.string.rateme__icon_content_description),
                        modifier = Modifier
                            .size(RateAppDialogDefaults.IconSize)
                            .padding(end = RateAppDialogDefaults.HorizontalSpacing),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MdText(
                        text = stringResource(R.string.rateme__dialog_first_message, appName),
                        color = RateAppDialogDefaults.BodyText,
                        fontSize = RateAppDialogDefaults.TextSize,
                    )
                    RateStarRow(
                        selectedStars = stars,
                        firstStarFocus = firstStarFocus,
                        onStarsChange = onStarsChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = RateAppDialogDefaults.DefaultPadding,
                                bottom = RateAppDialogDefaults.DefaultPadding,
                                end = RateAppDialogDefaults.SmallPadding,
                            ),
                    )
                }
            }
            if (showRate) {
                RateActionButton(
                    text = stringResource(R.string.rateme__dialog_first_button_rate),
                    isTv = isTv,
                    onClick = { onRate(stars) },
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = primaryActionFocus,
                )
            } else if (showThanks) {
                RateActionButton(
                    text = stringResource(R.string.rateme__dialog_first_thanks),
                    isTv = isTv,
                    onClick = onLater,
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = primaryActionFocus,
                )
            }
        }
    }
}

@Composable
private fun RateAppDialogTitleBar(
    showShareButton: Boolean,
    onShare: () -> Unit,
    onNever: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RateAppDialogDefaults.HeaderBackground)
            .padding(RateAppDialogDefaults.TitlePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MdText(
            text = stringResource(R.string.rateme__dialog_first_title),
            color = RateAppDialogDefaults.HeaderText,
            fontSize = RateAppDialogDefaults.TextSize,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (showShareButton) {
            RateTitleIconButton(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.rateme_cd_share),
                onClick = onShare,
            )
            Spacer(Modifier.width(RateAppDialogDefaults.HorizontalSpacing))
        }
        RateTitleIconButton(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.rateme_cd_close),
            onClick = onNever,
        )
    }
}

@Composable
private fun RateTitleIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier = Modifier
            .size(RateAppDialogDefaults.TitleButtonSize)
            .scale(if (focused) 1.15f else 1f)
            .then(
                if (focused) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        MdIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = RateAppDialogDefaults.IconTint,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun RateStarRow(
    selectedStars: Int,
    firstStarFocus: FocusRequester,
    onStarsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.focusGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.weight(1f))
        for (index in 1..5) {
            RateStar(
                index = index,
                filled = index <= selectedStars,
                focusRequester = if (index == 1) firstStarFocus else null,
                onClick = { onStarsChange(index) },
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RateStar(
    index: Int,
    filled: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    MdIcon(
        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
        contentDescription = stringResource(R.string.rateme_cd_star, index),
        tint = if (filled) RateAppDialogDefaults.StarFilled else RateAppDialogDefaults.StarEmpty,
        modifier = Modifier
            .size(40.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .scale(if (focused) 1.15f else 1f)
            .then(
                if (focused) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(2.dp),
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RateActionButton(
    text: String,
    isTv: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    val focusMod = modifier.then(
        if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier,
    )
    if (isTv) {
        TvButton(
            onClick = onClick,
            modifier = focusMod,
            shape = TvButtonDefaults.shape(shape = RectangleShape),
            colors = TvButtonDefaults.colors(
                containerColor = RateAppDialogDefaults.ButtonBackground,
                contentColor = RateAppDialogDefaults.ButtonText,
                focusedContainerColor = RateAppDialogDefaults.ButtonPressedBackground,
                focusedContentColor = RateAppDialogDefaults.ButtonText,
                pressedContainerColor = RateAppDialogDefaults.ButtonPressedBackground,
                pressedContentColor = RateAppDialogDefaults.ButtonText,
            ),
            scale = TvButtonDefaults.scale(focusedScale = 1.05f),
            border = TvButtonDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(2.dp, Color.White),
                    shape = RectangleShape,
                ),
            ),
        ) {
            TvText(
                text = text,
                fontSize = RateAppDialogDefaults.TextSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        MdButton(
            onClick = onClick,
            modifier = focusMod,
            shape = RectangleShape,
            colors = MdButtonDefaults.buttonColors(
                containerColor = if (pressed) {
                    RateAppDialogDefaults.ButtonPressedBackground
                } else {
                    RateAppDialogDefaults.ButtonBackground
                },
                contentColor = RateAppDialogDefaults.ButtonText,
            ),
            interactionSource = interaction,
        ) {
            MdText(
                text = text,
                fontSize = RateAppDialogDefaults.TextSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun isTelevision(): Boolean {
    val uiMode = LocalConfiguration.current.uiMode
    return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
}


/**
 * Stateless thank-you / feedback prompt that mirrors the legacy FeedbackDialog
 * (`com.androidsx.rateme.FeedbackDialog`, layouts `rateme__feedback_dialog_title`
 * / `rateme__feedback_dialog_message`).
 *
 * Shown after a below-threshold rating (1–4 stars). This composable does not
 * send email itself: [onWriteToDeveloper] is for the host (upcoming R6a
 * `RateUiState.ThankYou`). Cancel and Back call [onDismiss].
 * Does not depend on `:ratemyapp`. D-pad focus is implemented even though
 * the production rate flow is not shown on TV (`showOnTv = false`).
 */
@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RateThankYouDialog(
    visible: Boolean,
    onWriteToDeveloper: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showAppIcon: Boolean = false,
    appIcon: Painter? = null,
) {
    if (!visible) return

    val isTv = isTelevision()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        RateThankYouDialogContent(
            onWriteToDeveloper = onWriteToDeveloper,
            onDismiss = onDismiss,
            showAppIcon = showAppIcon,
            appIcon = appIcon,
            isTv = isTv,
            modifier = modifier,
        )
    }
}

/** Visual body of [RateThankYouDialog], extracted so previews can render it without a window. */
@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RateThankYouDialogContent(
    onWriteToDeveloper: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showAppIcon: Boolean = false,
    appIcon: Painter? = null,
    isTv: Boolean = false,
) {
    val primaryActionFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        primaryActionFocus.requestFocus()
    }

    val maxWidth = if (isTv) 560.dp else 400.dp
    Column(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth()
            .focusGroup()
            .focusRestorer(primaryActionFocus)
            .background(RateAppDialogDefaults.BodyBackground)
            .border(1.dp, RateAppDialogDefaults.HeaderBackground),
    ) {
        MdText(
            text = stringResource(R.string.rateme__dialog_first_title),
            color = RateAppDialogDefaults.HeaderText,
            fontSize = RateAppDialogDefaults.TextSize,
            modifier = Modifier
                .fillMaxWidth()
                .background(RateAppDialogDefaults.HeaderBackground)
                .padding(RateAppDialogDefaults.TitlePadding),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(RateAppDialogDefaults.BodyBackground)
                .padding(RateAppDialogDefaults.DefaultPadding),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                if (showAppIcon && appIcon != null) {
                    Image(
                        painter = appIcon,
                        contentDescription = stringResource(R.string.rateme__icon_content_description),
                        modifier = Modifier
                            .size(RateAppDialogDefaults.IconSize)
                            .padding(end = RateAppDialogDefaults.HorizontalSpacing),
                    )
                }
                MdText(
                    text = stringResource(R.string.rateme__dialog_feedback_message),
                    color = RateAppDialogDefaults.BodyText,
                    fontSize = RateAppDialogDefaults.TextSize,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RateAppDialogDefaults.DefaultPadding),
            ) {
                RateActionButton(
                    text = stringResource(android.R.string.cancel),
                    isTv = isTv,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Spacer(
                    Modifier.width(RateAppDialogDefaults.HorizontalSpacingSmall * 2),
                )
                RateActionButton(
                    text = stringResource(android.R.string.ok),
                    isTv = isTv,
                    onClick = onWriteToDeveloper,
                    modifier = Modifier.weight(1f),
                    focusRequester = primaryActionFocus,
                )
            }
        }
    }
}

/** Colors and sizes taken from RateMeDialog.Builder defaults and `rateme__*` dimens. */
object RateAppDialogDefaults {
    val HeaderBackground = Color.Black
    val HeaderText = Color.White
    val BodyBackground = Color.DarkGray
    val BodyText = Color.White
    val ButtonBackground = Color.Black
    val ButtonText = Color.White
    val ButtonPressedBackground = Color.Gray
    val IconTint = Color.White
    val StarFilled = Color.Yellow
    val StarEmpty = Color.White.copy(alpha = 0.85f)
    val DefaultPadding = 16.dp
    val SmallPadding = 8.dp
    val HorizontalSpacing = 16.dp
    val HorizontalSpacingSmall = 4.dp
    val TitlePadding = 12.dp
    val TextSize = 20.sp
    val IconSize = 88.dp
    val TitleButtonSize = 28.dp
}

@Composable
private fun PreviewRateAppDialog(initialStars: Int, showShareButton: Boolean = false) {
    var stars by remember { mutableIntStateOf(initialStars) }
    RateAppDialogContent(
        appName = stringResource(R.string.app_name_legacy),
        selectedStars = stars,
        onStarsChange = { stars = it },
        onRate = {},
        onLater = {},
        onNever = {},
        showShareButton = showShareButton,
    )
}

@Preview(name = "Phone light — empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Phone dark — empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RateAppDialogPhoneEmptyPreview() {
    PhoneTheme {
        PreviewRateAppDialog(initialStars = 0)
    }
}

@Preview(name = "Phone light — 5 stars", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Phone dark — 5 stars", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RateAppDialogPhoneRatedPreview() {
    PhoneTheme {
        PreviewRateAppDialog(initialStars = 5)
    }
}

@Preview(
    name = "TV 1080p — empty",
    device = "id:tv_1080p",
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RateAppDialogTvEmptyPreview() {
    TvTheme {
        RateAppDialogContent(
            appName = stringResource(R.string.app_name_legacy),
            selectedStars = 0,
            onStarsChange = {},
            onRate = {},
            onLater = {},
            onNever = {},
            isTv = true,
        )
    }
}

@Preview(
    name = "TV 1080p — 5 stars",
    device = "id:tv_1080p",
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RateAppDialogTvRatedPreview() {
    TvTheme {
        RateAppDialogContent(
            appName = stringResource(R.string.app_name_legacy),
            selectedStars = 5,
            onStarsChange = {},
            onRate = {},
            onLater = {},
            onNever = {},
            isTv = true,
        )
    }
}


@Preview(name = "Phone light — thank you", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Phone dark — thank you", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RateThankYouDialogPhonePreview() {
    PhoneTheme {
        RateThankYouDialogContent(
            onWriteToDeveloper = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "TV 1080p — thank you",
    device = "id:tv_1080p",
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RateThankYouDialogTvPreview() {
    TvTheme {
        RateThankYouDialogContent(
            onWriteToDeveloper = {},
            onDismiss = {},
            isTv = true,
        )
    }
}
