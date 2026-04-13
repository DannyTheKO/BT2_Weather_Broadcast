package sgu.homework.bt2_weather_broadcast.data.repository;

import retrofit2.Callback;
import sgu.homework.bt2_weather_broadcast.data.models.ForecastResponse;
import sgu.homework.bt2_weather_broadcast.data.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.data.remote.RetrofitClient;
import sgu.homework.bt2_weather_broadcast.data.remote.WeatherService;

public class WeatherRepository {
    private final WeatherService weatherService;
    private final String apiKey;

    public WeatherRepository(String apiKey) {
        this.apiKey = apiKey;
        this.weatherService = RetrofitClient.getService();
    }

    public void fetchWeather(String cityName, Callback<WeatherResponse> callback) {
        weatherService.getWeatherByCity(cityName, apiKey, "metric").enqueue(callback);
    }

    public void fetchForecast(String cityName, Callback<ForecastResponse> callback) {
        weatherService.getForecastByCity(cityName, apiKey, "metric").enqueue(callback);
    }

    public void fetchWeatherByCoords(double lat, double lon, Callback<WeatherResponse> callback) {
        weatherService.getWeatherByCoords(lat, lon, apiKey, "metric").enqueue(callback);
    }

    public void fetchForecastByCoords(double lat, double lon, Callback<ForecastResponse> callback) {
        weatherService.getForecastByCoords(lat, lon, apiKey, "metric").enqueue(callback);
    }
}
