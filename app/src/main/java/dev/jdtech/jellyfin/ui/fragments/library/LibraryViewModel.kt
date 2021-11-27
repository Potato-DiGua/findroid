package dev.jdtech.jellyfin.ui.fragments.library

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.repository.JellyfinRepository
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel
@Inject
constructor(
    private val jellyfinRepository: JellyfinRepository,
    private val application: Application
) : ViewModel() {
    val SP_KEY_SHOW_TYPE = "showType"

    private val sp = PreferenceManager.getDefaultSharedPreferences(application)

    private val _items = MutableLiveData<List<BaseItemDto>>()
    val items: LiveData<List<BaseItemDto>> = _items

    private val _finishedLoading = MutableLiveData<Boolean>()
    val finishedLoading: LiveData<Boolean> = _finishedLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _showType =
        MutableLiveData(ShowType.toShowType(sp.getString(SP_KEY_SHOW_TYPE, "")))
    val showType: LiveData<ShowType> = _showType


    fun loadItems(parentId: UUID, libraryType: String?) {
        _error.value = null
        _finishedLoading.value = false
        Timber.d("$libraryType")
        val itemType = when (libraryType) {
            "movies" -> "Movie"
            "tvshows" -> "Series"
            else -> null
        }
        viewModelScope.launch {
            try {
                _items.value = if (sp.getBoolean("show_folder", false)) {
                    jellyfinRepository.getItems(
                        parentId,
                        includeTypes = null,
                        recursive = false
                    )
                } else {
                    jellyfinRepository.getItems(
                        parentId,
                        includeTypes = if (itemType != null) listOf(itemType) else null,
                        recursive = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                _error.value = e.toString()
            }
            _finishedLoading.value = true
        }
    }

    fun changeShowType(type: ShowType) {
        _showType.postValue(type)
        sp.edit().putString(SP_KEY_SHOW_TYPE, type.toString()).apply()
    }

    enum class ShowType {
        LIST, GRID;

        companion object {
            fun toShowType(value: String?): ShowType {
                return try {
                    value?.let {
                        valueOf(value)
                    } ?: LIST

                } catch (ex: java.lang.Exception) {
                    // For error cases
                    LIST
                }
            }
        }

    }
}