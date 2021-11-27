package dev.jdtech.jellyfin.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jdtech.jellyfin.R

@Composable
fun ErrorDialogWithoutBorder(
    modifier: Modifier,
    onRetryClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .then(modifier)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_alert_circle),
            contentDescription = "",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.error_loading_data),
            style = MaterialTheme.typography.body1
        )
        Row(
            modifier = Modifier
                .wrapContentSize()
        ) {
            TextButton(onClick = onConfirmClick) {
                Text(
                    text = stringResource(id = R.string.view_details),
                    style = MaterialTheme.typography.body1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onRetryClick) {
                Text(
                    text = stringResource(id = R.string.retry),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}