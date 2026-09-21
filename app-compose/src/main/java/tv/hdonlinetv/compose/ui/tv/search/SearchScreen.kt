package tv.hdonlinetv.compose.ui.tv.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModel
import tv.hdonlinetv.compose.core.presentation.channel.SearchViewModelFactory
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.tv.components.TvEditableField

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
        onBack = { navController.popBackStack() },
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
    onBack: () -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = stringResource(R.string.ok))
        }
        Text(
            text = stringResource(R.string.search_hint),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        TvEditableField(
            value = query,
            onValueChange = onQueryChange,
            hint = stringResource(R.string.search_hint),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        ChannelGridBody(
            channels = results,
            isLoading = isLoading,
            onChannelClick = onChannelClick,
        )
    }
}
