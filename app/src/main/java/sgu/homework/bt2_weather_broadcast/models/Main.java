package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

public class Main {
    @SerializedName("temp")
    private float temp;

    @SerializedName("humidity")
    private int humidity;

    public float getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }
}
