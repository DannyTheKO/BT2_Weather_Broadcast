package sgu.homework.bt2_weather_broadcast.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.service.WeatherService;

public class WeatherRepository {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final WeatherService weatherService;
    private final String apiKey;

    public WeatherRepository(String apiKey) {
        this.apiKey = apiKey;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.weatherService = retrofit.create(WeatherService.class);
    }

    public void fetchWeather(String cityName, Callback<WeatherResponse> callback) {
        weatherService.getWeatherByCity(cityName, apiKey, "metric").enqueue(callback);
    }
}
