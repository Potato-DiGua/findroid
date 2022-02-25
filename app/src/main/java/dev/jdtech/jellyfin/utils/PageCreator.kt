package dev.jdtech.jellyfin.utils

import android.content.Intent
import android.net.Uri

object PageCreator {
    fun buildInnerIntent(path: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.Builder().scheme("findroid")
                .authority("www.findroid.com")
                .appendEncodedPath(path)
                .build()
        )
    }

    fun buildIjkPlayerIntent(): Intent {
        return buildInnerIntent("ijkplayer")
    }
}