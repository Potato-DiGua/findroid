package dev.jdtech.jellyfin.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import dev.jdtech.jellyfin.ui.fragments.mediainfo.MediaInfoViewModel
import java.lang.IllegalStateException

class VideoVersionDialogFragment(
    private val viewModel: MediaInfoViewModel
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = viewModel.item.value?.mediaSources?.map { it.name }
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Select a version")
                .setItems(items?.toTypedArray()) { _, which ->
                    viewModel.preparePlayerItems(which)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}