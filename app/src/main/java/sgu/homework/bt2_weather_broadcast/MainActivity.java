package sgu.homework.bt2_weather_broadcast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sgu.homework.bt2_weather_broadcast.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.repository.WeatherRepository;
import sgu.homework.bt2_weather_broadcast.utils.LocationHelper;

public class MainActivity extends AppCompatActivity {
    private EditText cityInput;
    private Button btnFetch;
    private Button btnLocationFetch;
    private TextView tvResult;
    
    private WeatherRepository weatherRepository;
    private LocationHelper locationHelper;

    private static final String API_KEY = "0478d572422957a478a1e94d0720cb7b";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize components
        cityInput = findViewById(R.id.cityInput);
        btnFetch = findViewById(R.id.btnFetch);
        btnLocationFetch = findViewById(R.id.btnLocationFetch);
        tvResult = findViewById(R.id.textView_output_weather);

        weatherRepository = new WeatherRepository(API_KEY);
        locationHelper = new LocationHelper(this);

        // Fetch weather by manual input
        btnFetch.setOnClickListener(v -> {
            String cityName = cityInput.getText().toString().trim();
            if (cityName.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            } else {
                fetchWeather(cityName);
            }
        });

        // Fetch weather by current location
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
        locationHelper.getCurrentCity(new LocationHelper.LocationCallback() {
            @Override
            public void onCityFound(String cityName) {
                cityInput.setText(cityName);
                fetchWeather(cityName);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeather(String cityName) {
        weatherRepository.fetchWeather(cityName, new Callback<WeatherResponse>() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndWeather();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
