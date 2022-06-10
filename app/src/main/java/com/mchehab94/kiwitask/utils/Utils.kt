package com.mchehab94.kiwitask.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.mchehab94.kiwitask.network.URLs
import java.text.SimpleDateFormat
import java.util.*


class Utils {
    companion object {

        /**
         * Helper method that takes a [timestamp] argument and formats it into 'dd/MM/yyy HH:mm'.
         *
         * @param timestamp
         * @return Date in 'dd/MM/yyy HH:mm' format
         */
        fun getDateTime(timestamp: Int): String {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
            return simpleDateFormat.format(timestamp * 1000L)
        }

        /**
         * Helper method that returns the proper image dimensions when sending request to 'images.kiwi.com'.
         * The image dimensions vary based on the device width. The value returned is one of the possible values:
         * - 300x165 (small, device width up-to 320dp)
         * - 600x330 (medium, device width up-to 600dp)
         * - 1280x720 (large, device width greater than 600dp)
         */
        @Composable
        fun getImageDimension(): String {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            if (screenWidth.value <= Constants.SMALL_WIDTH) {
                return Constants.SMALL_IMAGE
            }
            if (screenWidth.value <= Constants.MEDIUM_WIDTH) {
                return Constants.MEDIUM_IMAGE
            }
            return Constants.LARGE_IMAGE
        }

        /**
         * Helper method that properly formats endpoint for retrieving a destination image.
         * The endpoint is of the following format: BASE_IMAGE_URL/IMAGE_DIMENSIONS/ID.jpg
         *
         * @param id The id of the destination
         */
        @Composable
        fun getImageURL(id: String): String {
            return "${URLs.BASE_IMAGE_URL}/${getImageDimension()}/${id}.jpg"
        }

        /**
         * Helper method that wraps 'Text' compose UI element in a 'Box' so that the text is centered on the screen.
         *
         * @param text The text value to be displayed
         * @param fontSize Default value is 24f if none is passed.
         */
        @OptIn(ExperimentalUnitApi::class)
        @Composable
        fun CenterText(text: String, fontSize: Float = 24f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = text, fontSize = TextUnit(fontSize, TextUnitType.Sp), textAlign = TextAlign.Center)
            }
        }
    }
}