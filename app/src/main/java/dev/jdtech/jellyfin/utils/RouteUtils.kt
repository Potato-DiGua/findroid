package dev.jdtech.jellyfin.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import dev.jdtech.jellyfin.BaseApplication
import dev.jdtech.jellyfin.ui.activities.player.IjkPlayerActivityDirections
import dev.jdtech.jellyfin.ui.fragments.mediainfo.MediaInfoFragmentDirections
import org.jellyfin.sdk.model.api.BaseItemDto

object RouteUtils {

    fun navigateToIjkPlayerActivity(fragment: Fragment, item: BaseItemDto) {
        NavHostFragment.findNavController(fragment).navigate(
            IjkPlayerActivityDirections.actionGlobalIjkPlayerActivity(
                item.id,
                item.name,
                item.type ?: "Unknown"
            )
        )
    }

    fun navigateToMediaInfoFragment(fragment: Fragment, item: BaseItemDto) {
        NavHostFragment.findNavController(fragment).navigate(
            MediaInfoFragmentDirections.actionGlobalMediaInfoFragment(
                item.id,
                item.name,
                item.type ?: "Unknown"
            )
        )
    }

    fun navigateToVideoPage(fragment: Fragment, item: BaseItemDto) {
        if (PreferenceManager.getDefaultSharedPreferences(BaseApplication.INSTANCE)
                .getBoolean("ijk_player", false)
        ) {
            navigateToIjkPlayerActivity(fragment, item)
        } else {
            navigateToMediaInfoFragment(fragment, item)
        }
    }
}