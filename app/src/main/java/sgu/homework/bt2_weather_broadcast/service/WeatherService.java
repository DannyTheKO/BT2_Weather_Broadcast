package sgu.homework.bt2_weather_broadcast.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import sgu.homework.bt2_weather_broadcast.models.ForecastResponse;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // NEW: Method for 5-day / 3-hour forecast
    @GET("forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Optional: If you want to fetch by coordinates (useful for "Current Location")
    @GET("forecast")
    Call<ForecastResponse> getForecastByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("weather")
    Call<WeatherResponse> getWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

}
