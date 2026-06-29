package tv.hdonlinetv.compose.ui.tv.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModel
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody

@Composable
fun SearchScreen() {
    val navController = LocalTvNavController.current
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
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreenBody(
    query: String,
    results: List<ChannelUi>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.search_hint),
            modifier = Modifier.padding(48.dp, 24.dp),
        )
        ChannelGridBody(
            channels = results,
            isLoading = isLoading,
            onChannelClick = onChannelClick,
        )
    }
}
