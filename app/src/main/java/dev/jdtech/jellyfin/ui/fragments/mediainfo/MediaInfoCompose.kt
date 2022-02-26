package dev.jdtech.jellyfin.ui.fragments.mediainfo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.MdcTheme
import dev.jdtech.jellyfin.R
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun MediaInfo(viewModel: MediaInfoViewModel) {
    val item = viewModel.item.observeAsState()

    MdcTheme() {
        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
            Title(item)
            Button(viewModel)
        }
    }

}

@Composable
private fun Button(
    viewModel: MediaInfoViewModel,
) {
    val context = LocalContext.current
    val favorite = viewModel.favorite.observeAsState()
    val played = viewModel.played.observeAsState()
    Row() {
        Button(
            onClick = {
                if (viewModel.item.value?.remoteTrailers.isNullOrEmpty()) {
                    return@Button
                }

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(viewModel.item.value?.remoteTrailers?.get(0)?.url)
                )
                context.startActivity(intent)

            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.neutral_700)),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_film),
                contentDescription = stringResource(
                    id = R.string.trailer_button_description
                )
            )
        }
        Button(
            onClick = {
                if (viewModel.itemId != null) {
                    when (played.value) {
                        true -> viewModel.markAsUnplayed(viewModel.itemId!!)
                        false -> viewModel.markAsPlayed(viewModel.itemId!!)
                    }
                }

            }, shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.neutral_700)),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Image(
                painter =
                painterResource(id = if (played.value == true) R.drawable.ic_check_filled else R.drawable.ic_check),
                contentDescription = stringResource(
                    id = R.string.trailer_button_description
                )
            )
        }
        Button(
            onClick = {
                viewModel.itemId?.let {
                    when (favorite.value) {
                        true -> viewModel.unmarkAsFavorite(it)
                        false -> viewModel.markAsFavorite(it)
                    }
                }

            }, shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.neutral_700)),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Image(
                painter = painterResource(id = if (favorite.value == true) R.drawable.ic_heart_filled else R.drawable.ic_heart),
                contentDescription = stringResource(
                    id = R.string.favorite_button_description
                ),
                modifier = Modifier
                    .background(
                        colorResource(id = R.color.neutral_700),
                    )
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
private fun Title(item: State<BaseItemDto?>) {
    Text(text = item.value?.name ?: "", fontSize = 24.sp, color = Color.Black)
    Text(text = item.value?.originalTitle ?: "", fontSize = 14.sp, color = Color.Black)
}