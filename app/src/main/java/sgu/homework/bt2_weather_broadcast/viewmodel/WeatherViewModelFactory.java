package sgu.homework.bt2_weather_broadcast.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import sgu.homework.bt2_weather_broadcast.data.repository.WeatherRepository;

public class WeatherViewModelFactory implements ViewModelProvider.Factory {
    private final WeatherRepository repository;

    public WeatherViewModelFactory(WeatherRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WeatherViewModel.class)) {
            return (T) new WeatherViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
