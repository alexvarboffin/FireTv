package tv.hdonlinetv.compose.ui.mobile.playlist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageType
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModelFactory
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

@Composable
fun PlaylistManageScreen() {
    val navController = LocalPhoneNavController.current
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistManageViewModel = viewModel(
        factory = PlaylistManageViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.popBackStack()
        }
    }
    PlaylistManageScreenBody(
        type = state.type,
        title = state.title,
        url = state.url,
        username = state.username,
        password = state.password,
        useLocalFile = state.useLocalFile,
        isSaving = state.isSaving,
        titleError = state.titleError,
        urlError = state.urlError,
        usernameError = state.usernameError,
        passwordError = state.passwordError,
        onTypeChange = viewModel::onTypeChange,
        onTitleChange = viewModel::onTitleChange,
        onUrlChange = viewModel::onUrlChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLocalFileToggle = viewModel::onLocalFileToggle,
        onSave = viewModel::saveFromUrl,
        onSaveFile = viewModel::saveFromFile,
        onParseClipboard = viewModel::saveFromClipboard,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageScreenBody(
    type: PlaylistManageType,
    title: String,
    url: String,
    username: String,
    password: String,
    useLocalFile: Boolean,
    isSaving: Boolean,
    titleError: Boolean,
    urlError: Boolean,
    usernameError: Boolean,
    passwordError: Boolean,
    onTypeChange: (PlaylistManageType) -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLocalFileToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onSaveFile: (Uri) -> Unit,
    onParseClipboard: (String) -> Unit,
) {
    val context = LocalContext.current
    val playlistTypes = stringArrayResource(R.array.playlist_types)
    var typeExpanded by remember { mutableStateOf(false) }
    val selectedTypeLabel = playlistTypes[if (type == PlaylistManageType.M3U) 0 else 1]

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) onSaveFile(uri)
    }

    Scaffold(containerColor = colorResource(R.color.playlistManageBg),
        topBar = {
            LegacyTopAppBar(
                title = stringResource(R.string.playlist_management),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize().padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                LegacyFormField(
                    value = selectedTypeLabel,
                    onValueChange = {},
                    hint = selectedTypeLabel,
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                ) {
                    playlistTypes.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onTypeChange(if (index == 0) PlaylistManageType.M3U else PlaylistManageType.XTREAM)
                                typeExpanded = false
                            },
                        )
                    }
                }
            }

            LegacyFormField(
                value = title,
                onValueChange = onTitleChange,
                hint = stringResource(R.string.playlist_name),
                isError = titleError,
                modifier = Modifier.padding(top = 16.dp),
            )

            if (type == PlaylistManageType.M3U && useLocalFile) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(
                            colorResource(R.color.colorPrimaryDark),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        )
                        .clickable { filePicker.launch(arrayOf("*/*")) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.playlist_select_file),
                        color = colorResource(R.color.lightGray),
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                LegacyFormField(
                    value = url,
                    onValueChange = onUrlChange,
                    hint = stringResource(R.string.playlist_link),
                    isError = urlError,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            if (type == PlaylistManageType.M3U) {
                Text(
                    text = stringResource(R.string.cannotfind),
                    color = colorResource(R.color.colorAccent),
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            if (type == PlaylistManageType.XTREAM) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(
                            colorResource(R.color.colorPrimaryDark),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        )
                        .padding(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.username),
                        color = colorResource(R.color.lightGray),
                        fontSize = 14.sp,
                    )
                    LegacyFormField(
                        value = username,
                        onValueChange = onUsernameChange,
                        hint = stringResource(R.string.username_hint),
                        isError = usernameError,
                        modifier = Modifier.padding(top = 8.dp),
                        fieldBackground = colorResource(R.color.white),
                    )
                    Text(
                        text = stringResource(R.string.password),
                        color = colorResource(R.color.lightGray),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    LegacyFormField(
                        value = password,
                        onValueChange = onPasswordChange,
                        hint = stringResource(R.string.password_hint),
                        isError = passwordError,
                        modifier = Modifier.padding(top = 8.dp),
                        fieldBackground = colorResource(R.color.white),
                    )
                }
            }

            if (type == PlaylistManageType.M3U) {
                Text(
                    text = stringResource(R.string.local_storage),
                    color = colorResource(R.color.white),
                    modifier = Modifier.padding(top = 16.dp),
                )
                Switch(
                    checked = useLocalFile,
                    onCheckedChange = onLocalFileToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(R.color.colorAccent),
                        checkedTrackColor = colorResource(R.color.colorPrimaryDark),
                    ),
                )
            }

            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorAccent),
                    contentColor = colorResource(R.color.black),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.subscribe))
                }
            }

            if (type == PlaylistManageType.M3U) {
                Button(
                    onClick = { readClipboard(context)?.let(onParseClipboard) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorAccent),
                        contentColor = colorResource(R.color.white),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.parse_clipboard))
                }
            }
        }
    }
}

private fun readClipboard(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
    if (text.isNullOrBlank()) {
        Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show()
        return null
    }
    return text
}

@Composable
internal fun LegacyFormField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    readOnly: Boolean = false,
    fieldBackground: androidx.compose.ui.graphics.Color = colorResource(R.color.colorPrimaryDark),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isError) colorResource(R.color.colorLight) else fieldBackground,
                androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = colorResource(R.color.black),
            fontSize = 16.sp,
        ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    color = colorResource(R.color.lightGray),
                    fontSize = 16.sp,
                )
            }
            inner()
        },
    )
}
