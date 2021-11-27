package dev.jdtech.jellyfin.ui.fragments.library

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.jdtech.jellyfin.R
import dev.jdtech.jellyfin.api.JellyfinApi
import dev.jdtech.jellyfin.dialogs.ErrorDialogFragment
import dev.jdtech.jellyfin.ui.views.ErrorDialogWithoutBorder
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import kotlin.math.ceil

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private val viewModel: LibraryViewModel by viewModels()

    private val args: LibraryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme() {
                    Content(viewModel)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.library_menu, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadItems(args.libraryId, args.libraryType)
    }

    private fun navigateToMediaInfoFragment(item: BaseItemDto) {
        findNavController().navigate(
            LibraryFragmentDirections.actionLibraryFragmentToMediaInfoFragment(
                item.id,
                item.name,
                item.type ?: "Unknown"
            )
        )
    }

    private fun navigateToLibraryFragment(library: BaseItemDto) {
        findNavController().navigate(
            LibraryFragmentDirections.actionLibraryFragmentSelf(
                library.id,
                library.name,
                library.collectionType,
            )
        )
    }

    private fun onClick(item: BaseItemDto) {
        if (item.isFolder == true) {
            navigateToLibraryFragment(item)
        } else {
            navigateToMediaInfoFragment(item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_list -> {
                viewModel.changeShowType(LibraryViewModel.ShowType.LIST)
                true
            }
            R.id.action_show_gird -> {
                viewModel.changeShowType(LibraryViewModel.ShowType.GRID)
                true
            }
            else -> {
                false
            }
        }
    }

    @Composable
    fun Content(viewModel: LibraryViewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            val finishedLoading = viewModel.finishedLoading.observeAsState()
            if (finishedLoading.value != true) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            val error = viewModel.error.observeAsState()

            if (error.value != null) {
                ErrorDialogWithoutBorder(
                    modifier = Modifier.align(Alignment.Center),
                    onRetryClick = {
                        viewModel.loadItems(args.libraryId, args.libraryType)
                    },
                    onConfirmClick = {
                        ErrorDialogFragment(
                            viewModel.error.value ?: getString(R.string.unknown_error)
                        ).show(
                            parentFragmentManager,
                            "errordialog"
                        )
                    })
            } else {
                LibraryList(viewModel)
            }
        }
    }

    @Composable
    fun LibraryList(viewModel: LibraryViewModel) {
        val list = viewModel.items.observeAsState()
        val showType = viewModel.showType.observeAsState()
        val nColumns = integerResource(id = R.integer.library_columns)

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            list.value?.let {
                when (showType.value) {
                    LibraryViewModel.ShowType.GRID -> {
                        val rows = ceil(it.size / nColumns.toFloat()).toInt()

                        items(rows) { rowIndex ->
                            Row {
                                for (columnIndex in 0 until nColumns) {
                                    //itemIndex List数据位置
                                    val itemIndex = rowIndex * nColumns + columnIndex
                                    if (itemIndex >= it.size) {
                                        // 占位
                                        Box(
                                            modifier = Modifier.weight(
                                                (nColumns - columnIndex).toFloat(),
                                                fill = true
                                            )
                                        )
                                        break
                                    }

                                    val item = it[itemIndex]
                                    Box(modifier = Modifier.weight(1f, fill = true)) {
                                        ListItem(item = item, modifier = Modifier.width(50.dp)) {
                                            onClick(item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        items(it) { item ->
                            ListItem(item = item, modifier = null) {
                                onClick(item)
                            }
                        }

                    }
                }

            }
        }
    }

    @Composable
    fun ListItem(item: BaseItemDto, modifier: Modifier?, onClick: () -> Unit) {
        val jellyfinApi = JellyfinApi.getInstance(LocalContext.current, "")
        val itemId =
            if (item.type == "Episode" || item.type == "Season" && item.imageTags.isNullOrEmpty()) item.seriesId else item.id
        val img = jellyfinApi.api.baseUrl?.plus("/items/${itemId}/Images/${ImageType.PRIMARY}")

        Box(
            modifier = Modifier
                .padding(12.dp, 0.dp, 12.dp, 24.dp)
                .clickable {
                    onClick()
                }.apply {
                    if (modifier != null) {
                        then(modifier)
                    }
                }
        ) {
            Column() {
                Image(
                    painter = rememberImagePainter(img, builder = {
                        placeholder(R.color.neutral_800)
                        error(R.color.neutral_800)
                    }),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3 / 2f)
                        .clip(RoundedCornerShape(15.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = (if (item.type == "Episode") item.seriesName else item.name) ?: "",
                    modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp),
                    fontSize = 14.sp
                )
            }
            CornerSign(modifier = Modifier.align(Alignment.TopEnd), item)
        }
    }

    @Composable
    fun CornerSign(modifier: Modifier, item: BaseItemDto) {
        if (!(item.isFolder == true || item.userData?.played == true)) {
            return
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .padding(top = 8.dp, end = 8.dp)
                .background(
                    color = colorResource(id = R.color.primary),
                    shape = CircleShape
                )
                .then(modifier),
        ) {
            if (item.isFolder == true) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_folder_24),
                    contentDescription = stringResource(
                        id = R.string.episode_watched_indicator
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    contentScale = ContentScale.Inside
                )
            } else if (item.userData?.played == true) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = stringResource(
                        id = R.string.episode_watched_indicator
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    contentScale = ContentScale.Inside
                )
            }

        }
    }
}