package com.canopus.Vmusic.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canopus.Vmusic.R
import com.canopus.Vmusic.data.model.discovery.DiscoveryChannel
import com.canopus.Vmusic.util.ArtworkResolver

@Composable
fun ChannelCard(
    channel: DiscoveryChannel,
    onChannelClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val artworkUrl = remember(channel.id, channel.photoUrl) {
        // Prioritize the photoUrl if it exists, otherwise construct it from the ID.
        channel.photoUrl?.takeIf { it.isNotBlank() }
            ?: ArtworkResolver.getChannelPhotoUrl(channel.id)
    }


    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onChannelClicked(channel.id) }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(

                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .crossfade(true).build(),
                contentDescription = "Avatar for ${channel.name}",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = channel.englishName ?: channel.name,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}