package tv.hdonlinetv.compose.ui.mobile.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.DetailsViewModel
import tv.hdonlinetv.compose.core.presentation.channel.DetailsViewModelFactory
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.ui.components.RemoteImage
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun DetailsScreen() {
    val navController = LocalPhoneNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val channelId = args?.getLong(Routes.Details.ARG_CHANNEL_ID) ?: 0L
    val browseScope = PlayerBrowseScope.parse(
        type = args?.getString(Routes.Details.ARG_SCOPE),
        key = args?.getString(Routes.Details.ARG_SCOPE_KEY),
    )
    val repository = LocalChannelRepository.current
    val viewModel: DetailsViewModel = viewModel(
        factory = DetailsViewModelFactory(repository, channelId),
    )
    val state by viewModel.uiState.collectAsState()
    DetailsScreenBody(
        channel = state.channel,
        isLoading = state.isLoading,
        onBack = { navController.popBackStack() },
        onToggleFavorite = { viewModel.toggleFavorite() },
        onPlay = { navController.navigate(Routes.Player.build(channelId, browseScope)) },
    )
}

@Composable
fun DetailsScreenBody(
    channel: ChannelUi?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = channel?.name.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colorResource(R.color.black))
                    }
                },
                actions = {
                    if (channel != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = colorResource(R.color.colorPrimary),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.loading))
                }
            }
            channel == null -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.no_item))
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(colorResource(R.color.bgMain)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.details_hero_height)),
                    ) {
                        RemoteImage(
                            url = channel.cover,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(colorResource(R.color.playOverlay))
                                .clickable(onClick = onPlay),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(R.color.white),
                            )
                        }
                    }
                    Text(
                        text = channel.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.black),
                        modifier = Modifier.padding(10.dp),
                    )
                    if (!channel.category.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.details_category_label, channel.category.orEmpty()),
                            fontSize = 14.sp,
                            color = colorResource(R.color.lightGray),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                    Text(
                        text = channel.desc.orEmpty(),
                        fontSize = 16.sp,
                        color = colorResource(R.color.black),
                        modifier = Modifier.padding(10.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
