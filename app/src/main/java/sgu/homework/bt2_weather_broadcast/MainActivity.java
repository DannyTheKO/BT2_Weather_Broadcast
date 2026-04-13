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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sgu.homework.bt2_weather_broadcast.adapters.ForecastAdapter;
import sgu.homework.bt2_weather_broadcast.data.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.data.repository.WeatherRepository;
import sgu.homework.bt2_weather_broadcast.utils.LocationHelper;
import sgu.homework.bt2_weather_broadcast.viewmodel.WeatherViewModel;
import sgu.homework.bt2_weather_broadcast.viewmodel.WeatherViewModelFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private EditText cityInput;
    private TextView tvResult;
    private LocationHelper locationHelper;
    
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;
    private WeatherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        cityInput = findViewById(R.id.cityInput);
        Button btnFetch = findViewById(R.id.btnFetch);
        Button btnLocationFetch = findViewById(R.id.btnLocationFetch);
        tvResult = findViewById(R.id.textView_output_weather);
        rvForecast = findViewById(R.id.rvForecast);

        // Initialize Adapter
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        rvForecast.setAdapter(forecastAdapter);

        // Setup ViewModel
        WeatherRepository repository = new WeatherRepository(BuildConfig.openWeatherMap_API_KEY);
        WeatherViewModelFactory factory = new WeatherViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(WeatherViewModel.class);

        locationHelper = new LocationHelper(this);

        // Observe ViewModel Data
        observeViewModel();

        btnFetch.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (!city.isEmpty()) {
                viewModel.fetchWeatherAndForecast(city);
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

    private void observeViewModel() {
        viewModel.getWeatherData().observe(this, weatherData -> {
            if (weatherData != null) {
                updateWeatherUI(weatherData);
            }
        });

        viewModel.getForecastData().observe(this, forecastResponse -> {
            if (forecastResponse != null) {
                forecastAdapter.setForecastList(forecastResponse.getList());
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                tvResult.setText(error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                tvResult.setText("Loading...");
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weatherData) {
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
    }

    private void getCurrentLocationAndWeather() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationFound(double latitude, double longitude) {
                viewModel.fetchWeatherAndForecastByCoords(latitude, longitude);
            }

            @Override
            public void onCityFound(String cityName) {
                cityInput.setText(cityName);
            }

            @Override
            public void onError(String message) {
                tvResult.setText("Location error: " + message);
            }
        });
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
