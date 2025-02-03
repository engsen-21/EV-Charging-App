package smartcharge.master.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitOpenCharge {
    // Base URL for the Open Charge Map API
    private const val BASE_URL = "https://api.openchargemap.io/v3/"

    // Logging interceptor to log the HTTP request and response body
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set log level to log request and response bodies
    }

    // HTTP client with the logging interceptor added
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add the logging interceptor to the HTTP client
        .build() // Build the HTTP client


    // Lazy initialization of Retrofit instance
    // This ensures that the Retrofit instance is created only when it is first accessed
    val retrofit: Retrofit by lazy {
        // Set the base URL for the Retrofit instance
        Retrofit.Builder()
            // Set the base URL for the Retrofit instance
            .baseUrl(BASE_URL)
            // Set the HTTP client for the Retrofit instance
            .client(httpClient)
            // Add a converter factory for serialization and deserialization of objects
            .addConverterFactory(GsonConverterFactory.create())
            // Build the Retrofit instance
            .build()
    }
}

// The RetrofitOpenCharge object is a singleton that provides a configured instance of Retrofit for interacting with the Open Charge Map API.
// It includes a logging interceptor to log HTTP request and response bodies, and it uses lazy initialization to create the Retrofit instance only when it is first accessed.
// The Retrofit instance is configured with a base URL, an HTTP client, and a Gson converter factory for JSON serialization and deserialization.
