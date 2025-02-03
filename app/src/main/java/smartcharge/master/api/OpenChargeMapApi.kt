package smartcharge.master.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import smartcharge.master.data.ChargingStation

// Interface for the Open Charge Map API
interface OpenChargeMapApi {

    // Annotation for a GET request to the "poi" endpoint
    @GET("poi")
    fun getChargingStations(
        // Query parameter for the API key
        @Query("key") apiKey: String,
        // Query parameter for the country code, defaulting to "MY" (Malaysia)
        @Query("countrycode") countryCode: String = "MY",
        // Query parameter for the usage type ID, defaulting to 1 (public use)
        @Query("usagetypeid") usageTypeId: Int = 1,
        // Query parameter for the maximum number of results, defaulting to 1500
        @Query("maxresults") maxResults: Int = 1500
    ): Call<List<ChargingStation>> // Returns a Call object with a list of ChargingStation objects
}

// Interface for handling click events on charging stations
interface OnChargingStationClickListener {
    // Method to handle the click event, receiving the clicked ChargingStation as a parameter
    fun onChargingStationClick(station: ChargingStation)
}

//The OpenChargeMapApi interface defines a method for interacting with the Open Charge Map API, specifically for fetching a list of charging stations.
// It uses Retrofit annotations to specify the endpoint (@GET("poi")) and query parameters (@Query) with default values for country code, usage type ID, and maximum results.
// The method returns a Call object that will contain a list of ChargingStation objects.
//
//The OnChargingStationClickListener interface defines a single method for handling click events on charging stations.
// Implementing this interface allows us to specify the behavior when a charging station is clicked, receiving the clicked ChargingStation as a parameter.