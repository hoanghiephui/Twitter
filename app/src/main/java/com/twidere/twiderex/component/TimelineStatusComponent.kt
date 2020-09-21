package com.twidere.twiderex.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twidere.twiderex.db.model.DbMedia
import com.twidere.twiderex.db.model.DbStatusWithMedia
import com.twidere.twiderex.db.model.DbTimelineWithStatus
import com.twidere.twiderex.extensions.humanizedTimestamp

val standardPadding = 8.dp
val profileImageSize = 44.dp

@Composable
fun TimelineStatusComponent(
    data: DbTimelineWithStatus,
) {
    Column {
        val status = (data.retweet ?: data.status)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {})
                .padding(standardPadding),
        ) {
            if (data.retweet != null) {
                Row {
                    Box(
                        modifier = Modifier
                            .width(profileImageSize),
                        gravity = ContentGravity.CenterEnd,
                    ) {
                        Icon(asset = Icons.Default.Reply, tint = buttonContentColor())
                    }
                    Spacer(modifier = Modifier.width(standardPadding))
                    Text(
                        text = data.status.status.user.name + "retweet this tweet",
                        color = buttonContentColor()
                    )
                }
                Spacer(modifier = Modifier.height(standardPadding))
            }
            StatusComponent(
                status = status,
                quote = data.quote,
                showActions = true,
            )
            Spacer(modifier = Modifier.height(standardPadding))
            Row {
                Spacer(modifier = Modifier.width(profileImageSize))
                StatusActionButton(
                    icon = Icons.Default.Reply,
                    count = status.status.replyCount,
                    onClick = {},
                )
                StatusActionButton(
                    icon = Icons.Default.Comment,
                    count = status.status.retweetCount,
                    onClick = {},
                )
                StatusActionButton(
                    icon = Icons.Default.Favorite,
                    count = status.status.likeCount,
                    onClick = {},
                )
                TextButton(
                    onClick = {},
                    contentColor = buttonContentColor(),
                ) {
                    Icon(
                        asset = Icons.Default.Share,
                    )
                }
            }
        }
    }
}

@Composable
fun StatusComponent(
    status: DbStatusWithMedia,
    modifier: Modifier = Modifier,
    quote: DbStatusWithMedia? = null,
    showActions: Boolean = true,
) {
    Row(modifier = modifier) {
        NetworkImage(
            url = status.status.user.profileImage,
            modifier = Modifier
                .clip(CircleShape)
                .width(profileImageSize)
                .height(profileImageSize)
        )
        Spacer(modifier = Modifier.width(standardPadding))
        Column {
            Row {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = status.status.user.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0XFF4C9EEB)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${status.status.user.screenName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = EmphasisAmbient.current.medium.applyEmphasis(
                            contentColor()
                        ),
                    )
                }
                Row {
                    Text(text = status.status.timestamp.humanizedTimestamp())
                    if (showActions) {
                        Icon(
                            asset = Icons.Default.ArrowDropDown,
                            modifier = Modifier
                                .clickable(
                                    onClick = {},
                                ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = status.status.text)

            if (status.media.any()) {
                Spacer(modifier = Modifier.height(standardPadding))
                StatusMediaComponent(media = status.media.sortedBy { it.order })
            }

            if (!status.status.placeString.isNullOrEmpty()) {
                Row {
                    Icon(asset = Icons.Default.Place)
                    Text(text = status.status.placeString)
                }
            }

            if (quote != null) {
                Spacer(modifier = Modifier.height(standardPadding))
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            contentColor().copy(alpha = 0.12f),
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    StatusComponent(
                        status = quote,
                        showActions = false,
                        modifier = Modifier
                            .clickable(onClick = {})
                            .padding(standardPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun StatusMediaComponent(
    media: List<DbMedia>
) {
    if (media.size == 1) {
        val first = media.first()
        Box(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .aspectRatio(first.width.toFloat() / first.height.toFloat())
                .clip(RoundedCornerShape(8.dp))
        ) {
            first.previewUrl?.let { StatusMediaPreviewItem(url = it, onClick = {}) }
        }
    } else {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(270f / 162f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (media.size == 3) {
                Row {
                    media.firstOrNull()?.previewUrl?.let {
                        StatusMediaPreviewItem(
                            url = it,
                            modifier = Modifier.weight(1f),
                            onClick = {},
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        media.drop(1).forEach {
                            it.previewUrl?.let {
                                StatusMediaPreviewItem(
                                    url = it,
                                    modifier = Modifier.weight(1f),
                                    onClick = {},
                                )
                            }
                        }
                    }
                }
            } else {
                Column {
                    for (i in media.indices.filter { it % 2 == 0 }) {
                        Row(
                            modifier = Modifier.weight(1f),
                        ) {
                            for (y in (i until i + 2)) {
                                media.elementAtOrNull(y)?.let {
                                    it.previewUrl?.let {
                                        StatusMediaPreviewItem(
                                            url = it,
                                            modifier = Modifier.weight(1f),
                                            onClick = {},
                                        )
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

@Composable
fun StatusMediaPreviewItem(
    url: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        NetworkImage(
            url = url,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
fun StatusActionButton(
    modifier: Modifier = Modifier.weight(1f),
    icon: VectorAsset,
    count: Long,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
    ) {
        TextButton(
            onClick = onClick,
            contentColor = buttonContentColor(),
        ) {
            Icon(asset = icon)
            if (count > 0) {
                Box(modifier = Modifier.width(4.dp))
                Text(text = count.toString())
            }
        }
    }
}

@Composable
private fun buttonContentColor(): Color = EmphasisAmbient.current.medium.applyEmphasis(
    contentColor()
)