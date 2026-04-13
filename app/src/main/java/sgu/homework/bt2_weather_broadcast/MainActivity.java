package sgu.homework.bt2_weather_broadcast;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sgu.homework.bt2_weather_broadcast.adapters.ForecastAdapter;
import sgu.homework.bt2_weather_broadcast.models.ForecastResponse;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.repository.WeatherRepository;
import sgu.homework.bt2_weather_broadcast.utils.LocationHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private EditText cityInput;
    private TextView tvResult;
    private WeatherRepository weatherRepository;
    private LocationHelper locationHelper;
    
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        Button btnFetch = findViewById(R.id.btnFetch);
        Button btnLocationFetch = findViewById(R.id.btnLocationFetch);
        tvResult = findViewById(R.id.textView_output_weather);
        rvForecast = findViewById(R.id.rvForecast);

        // Initialize Adapter
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        rvForecast.setAdapter(forecastAdapter);

        // API Key
        weatherRepository = new WeatherRepository(BuildConfig.openWeatherMap_API_KEY);
        locationHelper = new LocationHelper(this);

        btnFetch.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherAndForecast(city);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        btnLocationFetch.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndWeather();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        });
    }

    private void getCurrentLocationAndWeather() {
        Log.d(TAG, "getCurrentLocationAndWeather called");
        tvResult.setText("Getting location...");
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationFound(double latitude, double longitude) {
                Log.d(TAG, "Location found: " + latitude + ", " + longitude);
                fetchWeatherByCoords(latitude, longitude);
                fetchForecastByCoords(latitude, longitude);
            }

            @Override
            public void onCityFound(String cityName) {
                cityInput.setText(cityName);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "LocationHelper error: " + message);
                tvResult.setText("Location error: " + message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherAndForecast(String cityName) {
        fetchWeather(cityName);
        fetchForecast(cityName);
    }

    private void fetchWeather(String cityName) {
        Log.d(TAG, "Fetching weather for city: " + cityName);
        tvResult.setText("Fetching weather for " + cityName + "...");
        weatherRepository.fetchWeather(cityName, new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                handleWeatherResponse(response);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "fetchWeather onFailure: " + t.getMessage());
                tvResult.setText("Network Failure: " + t.getMessage());
            }
        });
    }

    private void fetchForecast(String cityName) {
        weatherRepository.fetchForecast(cityName, new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastAdapter.setForecastList(response.body().getList());
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Log.e(TAG, "fetchForecast onFailure: " + t.getMessage());
            }
        });
    }

    private void fetchWeatherByCoords(double lat, double lon) {
        Log.d(TAG, "Fetching weather for coords: " + lat + ", " + lon);
        tvResult.setText("Fetching weather for your location...");
        weatherRepository.fetchWeatherByCoords(lat, lon, new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                handleWeatherResponse(response);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "fetchWeatherByCoords onFailure: " + t.getMessage());
                tvResult.setText("Network Failure: " + t.getMessage());
            }
        });
    }

    private void fetchForecastByCoords(double lat, double lon) {
        weatherRepository.fetchForecastByCoords(lat, lon, new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastAdapter.setForecastList(response.body().getList());
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Log.e(TAG, "fetchForecastByCoords onFailure: " + t.getMessage());
            }
        });
    }

    private void handleWeatherResponse(Response<WeatherResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            WeatherResponse weatherData = response.body();
            Log.d(TAG, "Weather response successful for: " + weatherData.getCityName());
            
            if (weatherData.getCityName() != null) {
                cityInput.setText(weatherData.getCityName());
            }

            float temp = weatherData.getMain().getTemp();
            int humidity = weatherData.getMain().getHumidity();
            float windSpeed = (weatherData.getWind() != null) ? weatherData.getWind().getSpeed() : 0;
            float rain = (weatherData.getRain() != null) ? weatherData.getRain().getH1() : 0;
            String description = weatherData.getWeather().get(0).getDescription();

            String info = "City: " + weatherData.getCityName() + "\n" +
                    "Temp: " + temp + "°C\n" +
                    "Humidity: " + humidity + "%\n" +
                    "Wind Speed: " + windSpeed + " m/s\n" +
                    "Rain (1h): " + rain + " mm\n" +
                    "Description: " + description;

            tvResult.setText(info);
        } else {
            String errorMsg = "Error: " + response.code() + " " + response.message();
            Log.e(TAG, "Weather response error: " + errorMsg);
            tvResult.setText(errorMsg + "\nCheck if your API key is active.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndWeather();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
