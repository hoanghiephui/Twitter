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
package com.twidere.twiderex.scenes.lists

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import com.twidere.twiderex.R
import com.twidere.twiderex.component.foundation.AppBar
import com.twidere.twiderex.component.foundation.AppBarNavigationButton
import com.twidere.twiderex.component.foundation.InAppNotificationScaffold
import com.twidere.twiderex.component.foundation.SwipeToRefreshLayout
import com.twidere.twiderex.component.lazy.collectAsLazyPagingItems
import com.twidere.twiderex.component.lazy.ui.LazyUiListList
import com.twidere.twiderex.di.assisted.assistedViewModel
import com.twidere.twiderex.ui.LocalActiveAccount
import com.twidere.twiderex.ui.TwidereScene
import com.twidere.twiderex.viewmodel.lists.ListsViewModel

// 1.Done finished layout of this page
// 2.Done finished ui style (size, padding, text)
// 3.Done bind viewmodel to this scene
// 4.TODO navigate to create list
// 5.TODO navigate to listTimes
// 6.TODO ADD this scene to route
// 7.TODO ADD TEXT RESOURCES

@Composable
fun ListsScene() {
    TwidereScene {
        InAppNotificationScaffold(
            topBar = {
                AppBar(
                    navigationIcon = {
                        AppBarNavigationButton()
                    },
                    title = {
                        Text(text = stringResource(id = R.string.scene_lists_title))
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = stringResource(
                            id = R.string.scene_lists_tabs_created
                        )
                    )
                }
            }
        ) {
            ListsContent()
        }
    }
}

@Composable
fun ListsContent() {
    val account = LocalActiveAccount.current ?: return
    // if list type is all , display title of each type
    val listsViewMode = assistedViewModel<ListsViewModel.AssistedFactory, ListsViewModel>(
        account,
    ) {
        it.create(account)
    }
    val ownerItems = listsViewMode.ownerSource.collectAsLazyPagingItems()
    val subscribeItems = listsViewMode.subscribedSource.collectAsLazyPagingItems()
    SwipeToRefreshLayout(
        refreshingState = ownerItems.loadState.refresh is LoadState.Loading,
        onRefresh = { ownerItems.refresh() }
    ) {
        LazyUiListList(
            listType = account.listType,
            ownerItems = ownerItems,
            subscribedItems = subscribeItems,
            onItemClicked = { /*to lists timeline page*/ }
        )
    }
}
