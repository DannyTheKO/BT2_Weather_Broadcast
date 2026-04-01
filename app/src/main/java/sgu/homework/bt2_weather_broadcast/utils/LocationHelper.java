package sgu.homework.bt2_weather_broadcast.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onCityFound(String cityName);
        void onError(String message);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentCity(LocationCallback callback) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String cityName = addresses.get(0).getLocality();
                                    if (cityName != null) {
                                        callback.onCityFound(cityName);
                                    } else {
                                        callback.onError("City not found");
                                    }
                                } else {
                                    callback.onError("No address found");
                                }
                            } catch (IOException e) {
                                callback.onError("Geocoder Error: " + e.getMessage());
                            }
                        } else {
                            callback.onError("Location not found");
                        }
                    })
                    .addOnFailureListener(e -> callback.onError("Location error: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onError("Permission denied: " + e.getMessage());
        }
    }
}
