package com.mchehab94.kiwitask.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.mchehab94.kiwitask.database.dao.*
import com.mchehab94.kiwitask.database.entities.*
import com.mchehab94.kiwitask.di.ApplicationScope
import com.mchehab94.kiwitask.database.entities.Flight
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Country::class, City::class, Airport::class, Flight::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao
    abstract fun flatCityDao(): CityDao
    abstract fun airportDao(): AirportDao
    abstract fun flightDao(): FlightDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope
        private val applicationScope: CoroutineScope,
        @ApplicationContext private val context: Context
    ) : RoomDatabase.Callback() {

        /**
         * Helper method that reads a file from [assets], taking [filename] as argument.
         *
         * @param fileName
         * @return An empty string if an error occurs, asset file content otherwise
         */
        private fun readAssets(fileName: String): String {
            try {
//                surprisingly, this approach is approximtaly 5 times faster compared to stream.bufferedReader().use(BufferedReader::readText)
//                this approach takes 14ms whereas buffered reader takes 70ms (measured using System.nanoTime)
                val stream = context.assets.open(fileName)
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                return String(buffer, Charset.defaultCharset())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val gson = Gson()
            val countryDao = database.get().countryDao()
            val flatCityDao = database.get().flatCityDao()
            val airportDao = database.get().airportDao()

            applicationScope.launch {
                val start = System.nanoTime()
                val json = readAssets("countriesCitiesAirports.json")
                val jsonArray = JSONArray(json)
                val countries = mutableListOf<Country>()
                val cities = mutableListOf<City>()
                val airports = mutableListOf<Airport>()
//                loop over countries first
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    val country = gson.fromJson(item.toString(), Country::class.java)
                    countries.add(country)

                    val citiesJSON = item.getJSONArray("cities")
//                    for each country, loop over its cities
                    for (cityIndex in 0 until citiesJSON.length()) {
                        val cityObject = citiesJSON.getJSONObject(cityIndex)
                        val airportsJSON = cityObject.getJSONArray("airports")
                        val city = gson.fromJson(cityObject.toString(), City::class.java)
                        cities.add(city)
//                        for each city, loop over its airports
                        for (airportIndex in 0 until airportsJSON.length()) {
                            val airport = gson.fromJson(
                                airportsJSON.getJSONObject(airportIndex).toString(),
                                Airport::class.java
                            )
                            airports.add(airport)
                        }
                    }
                }
//                insert all data in database
                countryDao.insertAll(countries)
                flatCityDao.insertAll(cities)
                airportDao.insertAll(airports)
                val end = System.nanoTime()
                Log.d("ON_CREATE_TIME", "${end - start}")
            }
        }
    }
}
