package com.mchehab94.kiwitask.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mchehab94.kiwitask.R
import kotlinx.coroutines.delay

@ExperimentalComposeUiApi
@Composable
fun <T> TextFieldWithDropdown(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    setValue: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    onSearch: () -> Unit,
    dropDownExpanded: Boolean,
    list: List<T>,
    label: String = "",
    onItemClick: (t: T) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    Column(modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused)
                        onDismissRequest()
                    else
                        keyboard?.show()
                },
            trailingIcon = {
                IconButton(onClick = { setValue(TextFieldValue("")) }) {
                    Icon(painterResource(R.drawable.ic_close), "")
                }
            },
            value = value,
            onValueChange = setValue,
            label = { Text(label) },
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            maxLines = 1
        )
        if (dropDownExpanded) {
            Card(elevation = 4.dp) {
                LazyColumn(modifier = Modifier.heightIn(0.dp, 300.dp)) {
                    items(list.size) { index ->
                        DropdownMenuItem(onClick = { onItemClick(list[index]) }) {
                            Text(text = list[index].toString(), color = Color.Black)
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
//        keyboard doesn't show immediately after navigating, some delay is added
        delay(500)
        keyboard?.show()
    }
}