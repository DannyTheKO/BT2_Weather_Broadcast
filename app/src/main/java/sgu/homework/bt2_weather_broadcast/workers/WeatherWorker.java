package sgu.homework.bt2_weather_broadcast.workers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;
import sgu.homework.bt2_weather_broadcast.BuildConfig;
import sgu.homework.bt2_weather_broadcast.data.models.Weather;
import sgu.homework.bt2_weather_broadcast.data.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.data.remote.RetrofitClient;
import sgu.homework.bt2_weather_broadcast.utils.NotificationHelper;

import java.io.IOException;

public class WeatherWorker extends Worker {
    private static final float TEMP_LIMIT_HIGH = 35.0f;
    private static final float TEMP_LIMIT_LOW = 10.0f;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_LAST_CITY = "last_city";

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cityName = prefs.getString(KEY_LAST_CITY, "Saigon");
        String apiKey = BuildConfig.openWeatherMap_API_KEY;

        // TEST NOTIFICATION: This will fire every time the worker runs
        NotificationHelper.showNotification(getApplicationContext(), 
            "Weather Check Init", 
            "Checking weather for " + cityName + "...");

        try {
            Response<WeatherResponse> response = RetrofitClient.getService()
                    .getWeatherByCity(cityName, apiKey, "metric")
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                checkWeatherAlerts(response.body());
                return Result.success();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }

        return Result.failure();
    }

    private void checkWeatherAlerts(WeatherResponse weatherData) {
        float temp = weatherData.getMain().getTemp();
        
        // Check Temperature Limits
        if (temp >= TEMP_LIMIT_HIGH) {
            NotificationHelper.showNotification(getApplicationContext(), 
                "High Temperature Alert", 
                "It's very hot! Current temperature is " + temp + "°C");
        } else if (temp <= TEMP_LIMIT_LOW) {
            NotificationHelper.showNotification(getApplicationContext(), 
                "Low Temperature Alert", 
                "It's quite cold! Current temperature is " + temp + "°C");
        }

        // Check Bad Weather (using OpenWeatherMap condition codes)
        // https://openweathermap.org/weather-conditions
        if (weatherData.getWeather() != null && !weatherData.getWeather().isEmpty()) {
            Weather weather = weatherData.getWeather().get(0);
            int id = weather.getId();
            
            // 2xx: Thunderstorm, 5xx: Rain, 6xx: Snow, 7xx: Atmosphere (Fog, Sand, etc.)
            if (id >= 200 && id < 300) {
                NotificationHelper.showNotification(getApplicationContext(), 
                    "Thunderstorm Warning", 
                    "Thunderstorm detected: " + weather.getDescription());
            } else if (id >= 500 && id < 600) {
                 NotificationHelper.showNotification(getApplicationContext(), 
                    "Rain Alert",
                    "Expect rain: " + weather.getDescription());
            } else if (id >= 600 && id < 700) {
                NotificationHelper.showNotification(getApplicationContext(), 
                    "Snow Alert", 
                    "Snow detected: " + weather.getDescription());
            }
        }
    }
}
