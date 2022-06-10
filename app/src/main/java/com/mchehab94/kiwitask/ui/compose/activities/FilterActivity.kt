package com.mchehab94.kiwitask.ui.compose.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.mchehab94.kiwitask.R
import com.mchehab94.kiwitask.ui.compose.components.TextFieldWithDropdown
import com.mchehab94.kiwitask.database.entities.City
import com.mchehab94.kiwitask.model.SelectedCityFilter
import com.mchehab94.kiwitask.ui.theme.KiwiTaskTheme
import com.mchehab94.kiwitask.viewmodel.FilterViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FilterActivity : ComponentActivity() {

    private val filterViewModel by viewModels<FilterViewModel>()

    val selectedCities = mutableStateListOf<City>()

    val dropDownOptions = mutableStateListOf<City>()
    val textFieldValue = mutableStateOf(TextFieldValue())
    val dropDownExpanded = mutableStateOf(false)

    val showErrorDialog = mutableStateOf(false)

    fun onDropdownDismissRequest() {
        dropDownExpanded.value = false
    }

    @ExperimentalComposeUiApi
    @Composable
    fun TextFieldWithDropdownUsage() {
        val context = LocalContext.current as? Activity
        val setValue: (tfv: TextFieldValue) -> Unit = {
            dropDownExpanded.value = true
            filterViewModel.searchQuery.value = it.text
            textFieldValue.value = it
            if (it.text.isEmpty()) {
                onDropdownDismissRequest()
            }
        }
        TextFieldWithDropdown(
            value = textFieldValue.value,
            setValue = setValue,
            onDismissRequest = ::onDropdownDismissRequest,
            onSearch = {
                dropDownExpanded.value = false
                applySearch(context)
            },
            dropDownExpanded = dropDownExpanded.value,
            list = dropDownOptions,
            label = stringResource(id = R.string.search_destinations),
            onItemClick = { city ->
                if (!selectedCities.contains(city)) {
                    selectedCities.add(city)
                }
            }
        )
    }

    fun canApplySearch(): Boolean {
        return selectedCities.size > 0
    }

    fun applySearch(context: Activity?) {
        if (!canApplySearch()) {
            showErrorDialog.value = true
            return
        }
        val intent = Intent()
        val cities = ArrayList<SelectedCityFilter>()
        selectedCities.forEach {
            cities.add(SelectedCityFilter(it.cityName, it.countryCode))
        }
        intent.putParcelableArrayListExtra("cities", cities)
        context?.setResult(Activity.RESULT_OK, intent)
        context?.finish()
    }

    @Composable
    fun FilterToolbar() {
        val context = LocalContext.current as? Activity
        TopAppBar(title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filter")
                Button(border = null,
                    elevation = elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                    onClick = {
                        applySearch(context)
                    }) {
                    Text("Apply")
                }
            }
        })
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RowChips() {
        FlowRow(
            mainAxisAlignment = MainAxisAlignment.Center,
            crossAxisSpacing = 8.dp
        ) {
            selectedCities.forEach { city ->
                Surface(
                    elevation = 4.dp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        colorResource(id = R.color.kiwi_green)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = city.cityName)
                        IconButton(
                            modifier = Modifier.size(12.dp, 12.dp),
                            onClick = {
                                selectedCities.filter { it.cityId == city.cityId }
                                    .forEach { selectedCities.remove(it) }
                            }) {
                            Icon(
                                painterResource(id = R.drawable.ic_close),
                                contentDescription = "",
                                Modifier.size(width = 12.dp, height = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @ExperimentalComposeUiApi
    @Composable
    fun FilterScreen() {
        KiwiTaskTheme {
            Scaffold(
                topBar = { FilterToolbar() },
                modifier = Modifier.fillMaxSize(),
                backgroundColor = colorResource(id = R.color.off_white),
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .animateContentSize()
                    ) {
                        val keyboard = LocalSoftwareKeyboardController.current
                        if (showErrorDialog.value) {
                            DisplayErrorDialog(
                                title = stringResource(R.string.error_title),
                                message = stringResource(R.string.no_selection_error)
                            ) {
                                showErrorDialog.value = false
                                keyboard?.show()
                            }
                        }
                        RowChips()
                        TextFieldWithDropdownUsage()
                    }
                }
            )
        }
    }

    @Composable
    fun DisplayErrorDialog(
        title: String,
        message: String,
        buttonCallback: () -> Unit
    ) {
        AlertDialog(onDismissRequest = { buttonCallback() },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = {
                    buttonCallback()
                }) {
                    Text("OK")
                }
            })
    }

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filterViewModel.filteredCities.observe(this) {
            dropDownOptions.clear()
            dropDownOptions.addAll(it)
            dropDownExpanded.value = true
        }

        setContent {
            FilterScreen()
        }
    }
}