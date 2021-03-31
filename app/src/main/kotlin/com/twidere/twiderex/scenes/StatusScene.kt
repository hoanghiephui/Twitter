/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.scenes

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.twidere.twiderex.R
import com.twidere.twiderex.component.foundation.AppBar
import com.twidere.twiderex.component.foundation.AppBarNavigationButton
import com.twidere.twiderex.component.foundation.InAppNotificationScaffold
import com.twidere.twiderex.component.lazy.LazyColumn2
import com.twidere.twiderex.component.lazy.collectAsLazyPagingItems
import com.twidere.twiderex.component.lazy.statusesIndexed
import com.twidere.twiderex.component.status.ExpandedStatusComponent
import com.twidere.twiderex.component.status.StatusDivider
import com.twidere.twiderex.component.status.TimelineStatusComponent
import com.twidere.twiderex.di.assisted.assistedViewModel
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.PlatformType
import com.twidere.twiderex.ui.LocalActiveAccount
import com.twidere.twiderex.ui.TwidereScene
import com.twidere.twiderex.viewmodel.mastodon.MastodonStatusViewModel
import com.twidere.twiderex.viewmodel.twitter.TwitterStatusViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot

@Composable
fun StatusScene(
    statusKey: MicroBlogKey,
) {
    val account = LocalActiveAccount.current ?: return
    val viewModel = when (account.type) {
        PlatformType.Twitter ->
            assistedViewModel<TwitterStatusViewModel.AssistedFactory, TwitterStatusViewModel>(
                statusKey,
                account,
            ) {
                it.create(account = account, statusKey = statusKey)
            }
        PlatformType.StatusNet -> TODO()
        PlatformType.Fanfou -> TODO()
        PlatformType.Mastodon ->
            assistedViewModel<MastodonStatusViewModel.AssistedFactory, MastodonStatusViewModel>(
                statusKey,
                account,
            ) {
                it.create(account = account, statusKey = statusKey)
            }
    }
    val source = viewModel.source.collectAsLazyPagingItems()
    val status by viewModel.status.observeAsState(initial = null)
    val state = rememberLazyListState()
    val distance = with(LocalDensity.current) {
        -50.dp.toPx()
    }
    LaunchedEffect(Unit) {
        snapshotFlow { source.loadState.refresh to source.snapshot() }
            .filter { it.first is LoadState.NotLoading }
            .filter { it.second.any() }
            .filterNot { it.second.indexOf(status) == -1 }
            .filter { state.firstVisibleItemScrollOffset == 0 && state.firstVisibleItemIndex == 0 }
            .collect {
                state.scrollToItem(it.second.indexOf(status))
                state.animateScrollBy(distance)
            }
    }

    TwidereScene {
        InAppNotificationScaffold(
            topBar = {
                AppBar(
                    title = {
                        Text(text = stringResource(id = R.string.scene_search_tabs_tweets))
                    },
                    navigationIcon = {
                        AppBarNavigationButton()
                    }
                )
            }
        ) {
            if (
                source.loadState.refresh is LoadState.NotLoading && source.itemCount > 0 ||
                source.loadState.refresh is LoadState.Loading || source.loadState.refresh is LoadState.Error
            ) {
                LazyColumn2(
                    state = state,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (source.loadState.refresh is LoadState.Loading || source.loadState.refresh is LoadState.Error) {
                        status?.let {
                            item(key = it.hashCode()) {
                                ExpandedStatusComponent(data = it)
                            }
                        }
                        if (source.loadState.refresh is LoadState.Loading) {
                            item {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        statusesIndexed(source) { index, it ->
                            it?.let { status ->
                                Column {
                                    if (status.statusKey == statusKey) {
                                        ExpandedStatusComponent(data = status)
                                    } else {
                                        TimelineStatusComponent(data = status)
                                    }
                                    if (index != source.itemCount - 1) {
                                        StatusDivider()
                                    } else {
                                        Spacer(modifier = Modifier.fillParentMaxHeight())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
