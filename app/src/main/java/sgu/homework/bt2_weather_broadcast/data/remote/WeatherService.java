package sgu.homework.bt2_weather_broadcast.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import sgu.homework.bt2_weather_broadcast.data.models.ForecastResponse;
import sgu.homework.bt2_weather_broadcast.data.models.WeatherResponse;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

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
