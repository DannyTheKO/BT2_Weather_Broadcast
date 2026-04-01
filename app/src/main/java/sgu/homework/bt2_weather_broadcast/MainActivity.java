package sgu.homework.bt2_weather_broadcast;

import java.io.IOException;
import java.util.*;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.service.WeatherService;

public class MainActivity extends AppCompatActivity {
    private EditText cityInput;
    private Button btnFetch;
    private Button btnLocationFetch;
    private TextView tvResult;
    private FusedLocationProviderClient fusedLocationClient;


    // TODO: Replace with your actual API key from OpenWeatherMap
    String API_KEY = "0478d572422957a478a1e94d0720cb7b";
    String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        cityInput = findViewById(R.id.cityInput);
        btnFetch = findViewById(R.id.btnFetch);
        btnLocationFetch = findViewById(R.id.btnLocationFetch);
        tvResult = findViewById(R.id.textView_output_weather);

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);

        // Set click listener
        btnFetch.setOnClickListener(v -> {
            String cityName = cityInput.getText().toString().trim();

            if (cityName.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            } else {
                fetchWeatherData(weatherService, cityName);
            }
        });

        // Location Fetch
        btnLocationFetch.setOnClickListener(v -> {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                // Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
                fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        tvResult.setText("Latitude: " + latitude + "\nLongitude: " + longitude);

                        Geocoder geocoder = new Geocoder(this, java.util.Locale.getDefault());

                        try {
                            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                            if (addresses != null && !addresses.isEmpty()) {
                                String cityName = addresses.get(0).getLocality();

                                if (cityName != null) {
                                    fetchWeatherData(weatherService, cityName);

                                    // Optional: Show the user what city was found
                                    Toast.makeText(this, "Location: " + cityName, Toast.LENGTH_SHORT).show();
                                    cityInput.setText(cityName);
                                } else {
                                    Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (IOException e) {
                            // This can happen if there is no internet or the geocoder service is down
                            tvResult.setText("Geocoder Error: " + e.getMessage());
                        }

                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                // 2. If not granted, request the permission from the user
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        });
    }

    private void fetchWeatherData(WeatherService service, String cityName) {
        service.getWeatherByCity(cityName, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherData = response.body();
                    String info = "City: " + weatherData.getCityName() + "\n" +
                            "Temp: " + weatherData.getMain().getTemp() + "°C\n" +
                            "Humidity: " + weatherData.getMain().getHumidity() + "%\n" +
                            "Description: " + weatherData.getWeather().get(0).getDescription();
                    tvResult.setText(info);
                } else {
                    tvResult.setText("Error: City not found or API problem.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvResult.setText("Network Failure: " + t.getMessage());
            }
        });
    }
}
