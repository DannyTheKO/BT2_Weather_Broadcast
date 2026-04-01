package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

public class Wind {
    @SerializedName("speed")
    private float speed;

    public float getSpeed() {
        return speed;
    }
}
