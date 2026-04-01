package sgu.homework.bt2_weather_broadcast.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
