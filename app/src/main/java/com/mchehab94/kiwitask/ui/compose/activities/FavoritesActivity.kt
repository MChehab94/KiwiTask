package com.mchehab94.kiwitask.ui.compose.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mchehab94.kiwitask.R
import com.mchehab94.kiwitask.database.entities.Flight
import com.mchehab94.kiwitask.utils.Utils
import com.mchehab94.kiwitask.viewmodel.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesActivity : ComponentActivity() {

    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val isLoading = mutableStateOf(true)
    private val groupedFlights =
        mutableStateOf<Map<String, List<Flight>>>(LinkedHashMap())
    private val searchQuery = mutableStateOf("")

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SearchField() {
        val (focusRequester) = FocusRequester.createRefs()
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
            trailingIcon = {
                IconButton(onClick = {
                    searchQuery.value = ""
                    favoritesViewModel.searchQuery.value = ""
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_close),
                        contentDescription = ""
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = searchQuery.value,
            label = { Text("Filter favorite destinations") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusRequester.freeFocus()
                    keyboardController?.hide()
                }
            ),
            maxLines = 1,
            onValueChange = {
                searchQuery.value = it
                favoritesViewModel.searchQuery.value = it
            })
    }

    @Composable
    fun RenderList(groupedFlights: MutableState<Map<String, List<Flight>>>) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            val keys = groupedFlights.value.keys
            items(groupedFlights.value.size) { index ->
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val key = keys.elementAt(index)
                    val flights = groupedFlights.value[key]
                    Text(
                        text = key,
                        fontSize = 24.sp
                    )
                    LazyRow {
                        items(flights!!.size) { index ->
                            val flight = flights[index]
                            val imageURL = Utils.getImageURL(flight.imageId)
                            Spacer(modifier = Modifier.size(8.dp))
                            Column {
                                Text(
                                    text = flight.cityTo,
                                    fontSize = 18.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(
                                    modifier = Modifier.size(
                                        height = 8.dp,
                                        width = 0.dp
                                    )
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageURL)
                                        .crossfade(true)
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.placeholder)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 16.dp)
                                        .aspectRatio(ratio = 2.0f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DisplayNoSearchResults() {
        Utils.CenterText(stringResource(id = R.string.no_favorites_found))
    }

    @Composable
    fun DisplayNoFavorites() {
        Utils.CenterText(stringResource(id = R.string.no_favorites))
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchQuery = mutableStateOf("")
        favoritesViewModel.filteredGroupedByCountry.observe(this) {
            groupedFlights.value = it
        }
        favoritesViewModel.searchQuery.observe(this) {
            searchQuery.value = it
        }
        favoritesViewModel.favoritesCount.observe(this) {
            lifecycleScope.launch {
//                simulating load
                delay(500)
                isLoading.value = false
            }
        }

        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = colorResource(id = R.color.off_white),
                content = { padding ->
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isLoading.value),
                        swipeEnabled = false,
                        onRefresh = { }) {
                        Column {
                            if (!isLoading.value) {
                                if (groupedFlights.value.isEmpty() && searchQuery.value.isEmpty()) {
                                    DisplayNoFavorites()
                                } else {
                                    SearchField()
                                    if (groupedFlights.value.isEmpty()) DisplayNoSearchResults()
                                    else RenderList(groupedFlights)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}