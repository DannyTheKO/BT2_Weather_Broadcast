package sgu.homework.bt2_weather_broadcast.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sgu.homework.bt2_weather_broadcast.data.models.ForecastResponse;
import sgu.homework.bt2_weather_broadcast.data.models.WeatherResponse;
import sgu.homework.bt2_weather_broadcast.data.repository.WeatherRepository;

public class WeatherViewModel extends ViewModel {
    private final WeatherRepository repository;

    private final MutableLiveData<WeatherResponse> weatherData = new MutableLiveData<>();
    private final MutableLiveData<ForecastResponse> forecastData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private String currentUnits = "metric";

    public WeatherViewModel(WeatherRepository repository) {
        this.repository = repository;
    }

    public LiveData<WeatherResponse> getWeatherData() {
        return weatherData;
    }

    public LiveData<ForecastResponse> getForecastData() {
        return forecastData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setUnits(String units) {
        this.currentUnits = units;
    }

    public String getUnits() {
        return currentUnits;
    }

    public void fetchWeatherAndForecast(String cityName) {
        isLoading.setValue(true);
        fetchWeather(cityName);
        fetchForecast(cityName);
    }

    public void fetchWeatherAndForecastByCoords(double lat, double lon) {
        isLoading.setValue(true);
        fetchWeatherByCoords(lat, lon);
        fetchForecastByCoords(lat, lon);
    }

    private void fetchWeather(String cityName) {
        repository.fetchWeather(cityName, currentUnits, new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherData.setValue(response.body());
                } else {
                    errorMessage.setValue("Weather Error: " + response.code());
                }
                checkLoadingFinished();
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                errorMessage.setValue("Weather Network Failure: " + t.getMessage());
                checkLoadingFinished();
            }
        });
    }

    private void fetchForecast(String cityName) {
        repository.fetchForecast(cityName, currentUnits, new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastData.setValue(response.body());
                } else {
                    errorMessage.setValue("Forecast Error: " + response.code());
                }
                checkLoadingFinished();
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                errorMessage.setValue("Forecast Network Failure: " + t.getMessage());
                checkLoadingFinished();
            }
        });
    }

    private void fetchWeatherByCoords(double lat, double lon) {
        repository.fetchWeatherByCoords(lat, lon, currentUnits, new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherData.setValue(response.body());
                } else {
                    errorMessage.setValue("Weather Error: " + response.code());
                }
                checkLoadingFinished();
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                errorMessage.setValue("Weather Network Failure: " + t.getMessage());
                checkLoadingFinished();
            }
        });
    }

    private void fetchForecastByCoords(double lat, double lon) {
        repository.fetchForecastByCoords(lat, lon, currentUnits, new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastData.setValue(response.body());
                } else {
                    errorMessage.setValue("Forecast Error: " + response.code());
                }
                checkLoadingFinished();
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                errorMessage.setValue("Forecast Network Failure: " + t.getMessage());
                checkLoadingFinished();
            }
        });
    }

    private void checkLoadingFinished() {
        isLoading.setValue(false);
    }
}
