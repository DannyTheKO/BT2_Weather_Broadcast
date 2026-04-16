package sgu.homework.bt2_weather_broadcast;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import sgu.homework.bt2_weather_broadcast.workers.WeatherWorker;

import java.util.concurrent.TimeUnit;

public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        scheduleWeatherChecks();
    }

    private void scheduleWeatherChecks() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Note: WorkManager enforces a minimum interval of 15 minutes for periodic work.
        // Setting it to 1 minute will be automatically clamped to 15 minutes by the system.
        PeriodicWorkRequest weatherWorkRequest = new PeriodicWorkRequest.Builder(
                WeatherWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WeatherAlerts",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherWorkRequest
        );
    }
}
