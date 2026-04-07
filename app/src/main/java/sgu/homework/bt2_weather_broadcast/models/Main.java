package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

public class Main {
    @SerializedName("temp")
    private float temp;

    @SerializedName("temp_min")
    private float temp_min;

    @SerializedName("temp_max")
    private float temp_max;

    @SerializedName("humidity")
    private int humidity;

    public float getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public float getTemp_min() { return temp_min; }

    public float getTemp_max() { return temp_max; }
}
