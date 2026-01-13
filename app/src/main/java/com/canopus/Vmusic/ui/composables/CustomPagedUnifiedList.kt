@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.canopus.Vmusic.ui.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.canopus.Vmusic.R
import com.canopus.Vmusic.domain.action.GlobalMediaActionHandler
import com.canopus.Vmusic.viewmodel.UnifiedDisplayItem
import timber.log.Timber

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomPagedUnifiedList(
    listKeyPrefix: String,
    items: List<UnifiedDisplayItem>,
    listState: LazyListState,
    actions: GlobalMediaActionHandler,
    onItemClicked: (UnifiedDisplayItem) -> Unit,
    isLoadingMore: Boolean,
    endOfList: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 80.dp),
    header: (@Composable () -> Unit)? = null,
) {

    val shouldLoadMore by remember {
        derivedStateOf {
            if (isLoadingMore || endOfList || items.isEmpty()) {
                Timber.d("CustomPagedUnifiedList ($listKeyPrefix): shouldLoadMore = false (isLoadingMore=$isLoadingMore, endOfList=$endOfList, isEmpty=${items.isEmpty()})")
                false
            } else {
                val layoutInfo = listState.layoutInfo
                val visibleItemsInfo = layoutInfo.visibleItemsInfo

                if (visibleItemsInfo.isEmpty()) {
                    false
                } else {
                    val lastVisibleItem = visibleItemsInfo.last()
                    val threshold = 3

                    val headerOffset = if (header != null) 1 else 0
                    val footerOffset = 1 // Footer is always present

                    val lastVisibleDataIndex = lastVisibleItem.index - headerOffset

                    val shouldTrigger = lastVisibleDataIndex >= items.size - 1 - threshold

                    Timber.d(
                        """
                        CustomPagedUnifiedList ($listKeyPrefix) Pagination Check:
                        - lastVisibleItem.index: ${lastVisibleItem.index}
                        - headerOffset: $headerOffset
                        - lastVisibleDataIndex: $lastVisibleDataIndex
                        - items.size: ${items.size}
                        - threshold: $threshold
                        - shouldTrigger: $shouldTrigger
                        - isLoadingMore: $isLoadingMore
                        - endOfList: $endOfList
                    """.trimIndent()
                    )

                    shouldTrigger
                }
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            Timber.i("CustomPagedUnifiedList ($listKeyPrefix): 🔄 TRIGGERING LOAD MORE")
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
        ) {
            header?.let {
                item(key = "${listKeyPrefix}_header") { it() }
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.stableId }
            ) { index, item ->
                UnifiedListItem(
                    item = item,
                    actions = actions,
                    onItemClick = {
                        onItemClicked(item)
                    }
                )
            }

            item(key = "${listKeyPrefix}_footer") {
                if (isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                } else if (endOfList && items.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.message_youve_reached_the_end),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}