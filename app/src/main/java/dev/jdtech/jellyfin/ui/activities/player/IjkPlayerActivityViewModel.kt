package dev.jdtech.jellyfin.ui.activities.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.models.PlayerItem
import dev.jdtech.jellyfin.repository.JellyfinRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
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

    fun onReleasePlayer(player: StandardGSYVideoPlayer) {
        mediaItems.value?.get(0)?.mediaId?.let {
            val id = it
            val time = player.gsyVideoManager.currentPosition.times(10000)
            MainScope().launch {
                try {
                    jellyfinRepository.postPlaybackStop(
                        UUID.fromString(id),
                        time
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

        }
    }


    fun pollPosition(player: StandardGSYVideoPlayer) {
        viewModelScope.launch {
            while (true) {
                mediaItems.value?.get(0)?.mediaId?.let {
                    try {
                        jellyfinRepository.postPlaybackProgress(
                            UUID.fromString(it),
                            player.gsyVideoManager.currentPosition.times(10000),
                            !player.gsyVideoManager.isPlaying
                        )
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
                delay(2000)
            }
        }
    }


    fun startPlay() {
        mediaItems.value?.get(0)?.mediaId?.let {
            Timber.d("Playing MediaItem: $it")
            viewModelScope.launch {
                try {
                    jellyfinRepository.postPlaybackStart(UUID.fromString(it))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}