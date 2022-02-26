package dev.jdtech.jellyfin.ui.fragments.library

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.models.ShowType
import dev.jdtech.jellyfin.models.SortType
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
    private val application: Application,
    private val sp: SharedPreferences
) : ViewModel() {
    companion object {
        const val SP_KEY_SHOW_TYPE = "showType"
        const val SP_KEY_SORT_TYPE = "sortType"
        const val SP_KEY_SORT_REVERSE = "sortReverse"
    }


    private val _items = MutableLiveData<List<BaseItemDto>>()
    val items: LiveData<List<BaseItemDto>> = _items

    private val _finishedLoading = MutableLiveData<Boolean>()
    val finishedLoading: LiveData<Boolean> = _finishedLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _showType =
        MutableLiveData(ShowType.toShowType(sp.getString(SP_KEY_SHOW_TYPE, "")))
    val showType: LiveData<ShowType> = _showType

    private val _sortType =
        MutableLiveData(SortType.parse(sp.getString(SP_KEY_SORT_TYPE, "")))
    val sortType: LiveData<SortType> = _sortType

    private val _sortReverse = MutableLiveData(sp.getBoolean(SP_KEY_SORT_REVERSE, false))
    val sortReverse: LiveData<Boolean> = _sortReverse

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    // 原数据
    var origin: List<BaseItemDto>? = null


    fun loadItems(parentId: UUID, libraryType: String?, refreshing: Boolean = false) {
        _error.value = null

        if (refreshing) {
            _isRefreshing.value = true
        } else {
            _finishedLoading.value = false
        }
        Timber.d("$libraryType")
        val itemType = when (libraryType) {
            "movies" -> "Movie"
            "tvshows" -> "Series"
            else -> null
        }
        viewModelScope.launch {
            try {
                origin = if (sp.getBoolean("show_folder", false)) {
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

                sortList(sortType.value ?: SortType.DEFAULT, sortReverse.value ?: false)
            } catch (e: Exception) {
                Timber.e(e)
                _error.value = e.toString()
            }
            _finishedLoading.value = true
            _isRefreshing.value = false
        }
    }

    fun changeShowType(type: ShowType) {
        _showType.postValue(type)
        sp.edit().putString(SP_KEY_SHOW_TYPE, type.toString()).apply()
    }

    fun changeSortType(type: SortType, reverse: Boolean) {
        _sortType.postValue(type)
        _sortReverse.postValue(reverse)
        sp.edit()
            .putString(SP_KEY_SORT_TYPE, type.name)
            .putBoolean(SP_KEY_SORT_REVERSE, reverse)
            .apply()
    }

    fun sortList(type: SortType, reverse: Boolean) {
        if (origin == null) {
            return
        }

        if (type == SortType.DEFAULT) {
            _items.value = origin
            return
        }

        _items.value = when (type) {
            SortType.NAME -> origin!!.sortedWith { a, b ->
                val result = if (a.isFolder == true && b.isFolder != true) {
                    -1
                } else if (a.isFolder != true && b.isFolder == true) {
                    1
                } else {
                    (a.name ?: "").compareTo(b.name ?: "")
                }
                return@sortedWith if (reverse) -result else result
            }
            SortType.DEFAULT -> sortList(origin!!, reverse) { it.name }
        }
    }

    private fun <T, R : Comparable<R>> sortList(
        list: List<T>,
        reverse: Boolean,
        selector: (T) -> R?
    ): List<T> {
        return if (!reverse) {
            list.sortedBy(selector)
        } else {
            list.sortedByDescending(selector)
        }
    }
}