package tv.hdonlinetv.compose.ui.mobile.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModel
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModelFactory
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

@Composable
fun SearchScreen() {
    val navController = LocalPhoneNavController.current
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    val settings = settingsRepository.getSettings()
    SearchScreenBody(
        query = state.query,
        results = state.results,
        isLoading = state.isLoading,
        onQueryChange = viewModel::onQueryChange,
        onBack = { navController.popBackStack() },
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}

@Composable
fun SearchScreenBody(
    query: String,
    results: List<ChannelUi>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = stringResource(R.string.search_hint),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(R.color.bgMain)),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_hint),
                        color = colorResource(R.color.searchHint),
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorResource(R.color.tabText),
                    unfocusedTextColor = colorResource(R.color.tabText),
                ),
            )
            ChannelGridBody(
                channels = results,
                isLoading = isLoading,
                onChannelClick = onChannelClick,
            )
        }
    }
}
