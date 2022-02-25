package dev.jdtech.jellyfin.ui.activities.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.models.PlayerItem
import dev.jdtech.jellyfin.repository.JellyfinRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class IjkPlayerActivityViewModel
@Inject
constructor(
    private val jellyfinRepository: JellyfinRepository
) : ViewModel() {
    private val _mediaItems = MutableLiveData<List<MediaItem>>()
    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    fun initializePlayer(
        items: List<PlayerItem>
    ) {
        viewModelScope.launch {
            val mediaItems: MutableList<MediaItem> = mutableListOf()
            try {
                for (item in items) {
                    val streamUrl = jellyfinRepository.getStreamUrl(item.itemId, item.mediaSourceId)
                    Timber.d("Stream url: $streamUrl")
                    val mediaItem =
                        MediaItem.Builder()
                            .setMediaId(item.itemId.toString())
                            .setUri(streamUrl)
                            .build()
                    mediaItems.add(mediaItem)
                }
                _mediaItems.value = mediaItems
            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }
}