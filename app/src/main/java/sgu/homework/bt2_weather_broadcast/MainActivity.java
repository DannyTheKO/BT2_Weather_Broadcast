package sgu.homework.bt2_weather_broadcast;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import com.google.android.material.button.MaterialButtonToggleGroup;

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
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_LAST_CITY = "last_city";
    private static final String KEY_UNITS = "units";

    private EditText cityInput;
    private TextView tvLog;
    private TextView tvCity;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvRain;
    private TextView tvDescription;

    private LocationHelper locationHelper;
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;
    private WeatherViewModel viewModel;
    private SharedPreferences prefs;
    private MaterialButtonToggleGroup unitToggleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize UI
        cityInput = findViewById(R.id.cityInput);
        Button btnFetch = findViewById(R.id.btnFetch);
        Button btnLocationFetch = findViewById(R.id.btnLocationFetch);

        tvLog = findViewById(R.id.textView_output_log);
        tvCity = findViewById(R.id.textView_output_weather_cityName);
        tvTemp = findViewById(R.id.textView_output_weather_temperature);
        tvHumidity = findViewById(R.id.textView_output_weather_humidity);
        tvWindSpeed = findViewById(R.id.textView_output_weather_windSpeed);
        tvRain = findViewById(R.id.textView_output_weather_rain);
        tvDescription = findViewById(R.id.textView_output_weather_description);

        rvForecast = findViewById(R.id.rvForecast);
        unitToggleGroup = findViewById(R.id.unitToggleGroup);

        // Initialize Adapter
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        rvForecast.setAdapter(forecastAdapter);

        // Setup ViewModel
        WeatherRepository repository = new WeatherRepository(BuildConfig.openWeatherMap_API_KEY);
        WeatherViewModelFactory factory = new WeatherViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(WeatherViewModel.class);

        // Load saved units
        String savedUnits = prefs.getString(KEY_UNITS, "metric");
        viewModel.setUnits(savedUnits);
        if (savedUnits.equals("imperial")) {
            unitToggleGroup.check(R.id.btnFahrenheit);
        } else {
            unitToggleGroup.check(R.id.btnCelsius);
        }

        locationHelper = new LocationHelper(this);

        // Observe ViewModel Data
        observeViewModel();

        // Check Notification Permission for Android 13+
        checkNotificationPermission();

        // Load last searched city
        String lastCity = prefs.getString(KEY_LAST_CITY, "");
        if (!lastCity.isEmpty()) {
            cityInput.setText(lastCity);
            viewModel.fetchWeatherAndForecast(lastCity);
        }

        btnFetch.setOnClickListener(v -> {
            performSearch();
        });

        cityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
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

        unitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String newUnits = (checkedId == R.id.btnFahrenheit) ? "imperial" : "metric";
                if (!newUnits.equals(viewModel.getUnits())) {
                    viewModel.setUnits(newUnits);
                    prefs.edit().putString(KEY_UNITS, newUnits).apply();

                    // Refresh data with new units
                    String city = cityInput.getText().toString().trim();
                    if (!city.isEmpty()) {
                        viewModel.fetchWeatherAndForecast(city);
                    }
                }
            }
        });
    }

    private void performSearch() {
        String city = cityInput.getText().toString().trim();
        if (!city.isEmpty()) {
            viewModel.fetchWeatherAndForecast(city);
            hideKeyboard();
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void observeViewModel() {
        viewModel.getWeatherData().observe(this, weatherData -> {
            if (weatherData != null) {
                updateWeatherUI(weatherData);
                // Save city name on success
                if (weatherData.getCityName() != null) {
                    prefs.edit().putString(KEY_LAST_CITY, weatherData.getCityName()).apply();
                }
            }
        });

        viewModel.getForecastData().observe(this, forecastResponse -> {
            if (forecastResponse != null) {
                String unitSymbol = viewModel.getUnits().equals("imperial") ? "°F" : "°C";
                forecastAdapter.setUnitSymbol(unitSymbol);
                forecastAdapter.setForecastList(forecastResponse.getList());
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                tvLog.setText(error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                tvLog.setText("Loading...");
            } else {
                tvLog.setText("Fetch Complete!");
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

        String unitSymbol = viewModel.getUnits().equals("imperial") ? "°F" : "°C";
        String windUnit = viewModel.getUnits().equals("imperial") ? "mph" : "m/s";

        String cityName = "City Name: " + weatherData.getCityName();
        String temperature = "Temperature: " + temp + unitSymbol;
        String humidityText = "Humidity: " + humidity + "%";
        String windSpeedText = "Wind Speed: " + windSpeed + " " + windUnit;
        String rainText = "Rain (last hour): " + rain + "mm";

        tvCity.setText(cityName);
        tvTemp.setText(temperature);
        tvHumidity.setText(humidityText);
        tvWindSpeed.setText(windSpeedText);
        tvRain.setText(rainText);
        tvDescription.setText(description);
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
                tvLog.setText("Location error: " + message);
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
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. You won't see weather alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
