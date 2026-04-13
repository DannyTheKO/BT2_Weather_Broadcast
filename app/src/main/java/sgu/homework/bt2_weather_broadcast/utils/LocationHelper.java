package sgu.homework.bt2_weather_broadcast.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface LocationCallback {
        void onLocationFound(double latitude, double longitude);
        void onCityFound(String cityName);
        void onError(String message);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(LocationCallback callback) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                            // Notify that coordinates are found - this is enough for the Weather API!
                            callback.onLocationFound(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.w(TAG, "Location is null");
                            callback.onError("Location not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getCurrentLocation failure", e);
                        callback.onError("Location error: " + e.getMessage());
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: permission denied", e);
            callback.onError("Permission denied: " + e.getMessage());
        }
    }

    // Optional: Only call this manually if you specifically need Android's Geocoder
    public void getCityNameFromLocation(Location location, LocationCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onError("Geocoder not available");
            return;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        final boolean[] isHandled = {false};
        Runnable timeoutRunnable = () -> {
            if (!isHandled[0]) {
                isHandled[0] = true;
                callback.onError("Geocoder timeout");
            }
        };
        mainHandler.postDelayed(timeoutRunnable, 5000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                    if (!isHandled[0]) {
                        isHandled[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        mainHandler.post(() -> processAddresses(addresses, callback));
                    }
                }
                @Override
                public void onError(String error) {
                    if (!isHandled[0]) {
                        isHandled[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        mainHandler.post(() -> callback.onError(error));
                    }
                }
            });
        } else {
            executorService.execute(() -> {
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!isHandled[0]) {
                        isHandled[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        mainHandler.post(() -> processAddresses(addresses, callback));
                    }
                } catch (Exception e) {
                    if (!isHandled[0]) {
                        isHandled[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        mainHandler.post(() -> callback.onError(e.getMessage()));
                    }
                }
            });
        }
    }

    private void processAddresses(List<Address> addresses, LocationCallback callback) {
        if (addresses != null && !addresses.isEmpty()) {
            String city = addresses.get(0).getLocality();
            if (city == null) city = addresses.get(0).getAdminArea();
            if (city != null) {
                callback.onCityFound(city);
            } else {
                callback.onError("City name not found");
            }
        }
    }

    public void getCurrentCity(LocationCallback callback) {
        getCurrentLocation(callback);
    }
}
