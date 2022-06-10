package com.mchehab94.kiwitask.ui.compose.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mchehab94.kiwitask.R
import com.mchehab94.kiwitask.database.entities.Flight
import com.mchehab94.kiwitask.model.SelectedCityFilter
import com.mchehab94.kiwitask.ui.theme.KiwiTaskTheme
import com.mchehab94.kiwitask.utils.Utils
import com.mchehab94.kiwitask.viewmodel.FlightViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val flightViewModel by viewModels<FlightViewModel>()
    private var flightList = mutableStateOf(mutableListOf<Flight>())

    private val displayError = mutableStateOf(false)
    private val errorMessage = mutableStateOf("")

    private var didRefresh = mutableStateOf(false)

    @Composable
    fun Toolbar() {
        val context = LocalContext.current
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    didRefresh.value = true
                    val intent = it.data
                    if (intent != null) {
                        val cities =
                            intent.getParcelableArrayListExtra<SelectedCityFilter>("cities")
                        cities?.let {
                            flightViewModel.getRetrofitFlights(cities)
                        }
                    }
                }
            }
        TopAppBar(title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.main_activity_title))
                Row {
                    IconButton(onClick = {
                        val intent = Intent(context, FavoritesActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_favorite),
                            ""
                        )
                    }
                    IconButton(onClick = {
                        launcher.launch(Intent(context, FilterActivity::class.java))
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_flights_filter),
                            stringResource(R.string.filter_icon_content_description)
                        )
                    }
                }
            }
        })
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun NoFlights() {
        Utils.CenterText(errorMessage.value)
    }

    @ExperimentalPagerApi
    @Composable
    fun ViewPagerTest(list: List<Flight>, pagerState: PagerState) {
        if (list.isEmpty()) {
            return
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentHeight()
        ) {
            HorizontalPager(
                count = list.size,
                modifier = Modifier.padding(vertical = 16.dp),
                state = pagerState
            ) { page ->
                val flight = list[page]
                val imageURL =
                    if (flight.imageId.isEmpty()) "" else Utils.getImageURL(
                        flight.imageId
                    )
                Log.d("IMAGE URL", imageURL)
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
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
                            .aspectRatio(ratio = 2.0f)
                    )
                    BottomImageRow(flight.cityTo, flight.price, flight.currency)
                    FlightInfo(flight)
                }
            }
            RenderPagerIndicator(pagerState)
        }
    }

    @Composable
    fun BottomImageRow(destination: String, price: Int, curerncy: String) {
        val fontSize = 18.sp
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = destination,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
            Text(
                text = "$price${Currency.getInstance(curerncy).symbol}",
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }

    @Composable
    fun FlightInfo(flight: Flight) {
        val tintColor = Color(0xFF04AC9C)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = "Departure: ${Utils.getDateTime(flight.departureTime)}")
            Text(text = "Arrival: ${Utils.getDateTime(flight.arrivalTime)}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${stringResource(R.string.flight_duration)}: ${flight.duration}")
                IconButton(onClick = {
                    flightViewModel.onFavoriteClick(flight)
                }) {
                    Icon(
                        painterResource(id = if (flight.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite),
                        contentDescription = stringResource(R.string.add_to_favorites),
                        tint = tintColor,
                    )
                }
            }
        }
    }

    @ExperimentalPagerApi
    @Composable
    fun RenderPagerIndicator(pagerState: PagerState) {
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(16.dp),
        )
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun observeResponse(pagerState: PagerState) {
        val scope = rememberCoroutineScope()
        flightViewModel.getKiwiResponse().observe(this) { kiwiResponse ->
            flightList.value = kiwiResponse.data as MutableList<Flight>
            if (didRefresh.value) {
                scope.launch(Dispatchers.Main) {
                    pagerState.scrollToPage(0, pageOffset = 0f)
                }
                didRefresh.value = false
            }
            displayError.value = false
        }
    }

    private fun observeErrorMessage() {
        flightViewModel.errorMessage.observe(this) { errorResponse ->
            if (errorResponse == null) {
                return@observe
            }
            errorMessage.value = when (errorResponse) {
                FlightViewModel.ResponseStatus.NoFlightsFound -> {
                    getString(R.string.no_flights_found)
                }
                FlightViewModel.ResponseStatus.GenericError -> {
                    getString(R.string.generic_error_message)
                }
                FlightViewModel.ResponseStatus.NetworkTimeout -> {
                    getString(R.string.network_timeout_message)
                }
                else -> {
//                            display a default message
                    getString(R.string.generic_error_message)
                }
            }
            displayError.value = true
        }
    }

    private fun observeRefresh() {
        flightViewModel.didRefresh.observe(this) {
            didRefresh.value = it
        }
    }

    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pagerState = rememberPagerState()

            KiwiTaskTheme {

                observeResponse(pagerState)
                observeErrorMessage()
                observeRefresh()

                rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = colorResource(id = R.color.off_white),
                    topBar = {
                        Toolbar()
                    },
                    content = { padding ->
                        SwipeRefresh(
                            state = rememberSwipeRefreshState(flightViewModel.isNetworkRunning.value),
                            onRefresh = {
                                flightViewModel.refresh()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            if (displayError.value) {
                                NoFlights()
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxSize()
                                ) {
                                    ViewPagerTest(flightList.value, pagerState)
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}