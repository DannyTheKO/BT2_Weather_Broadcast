package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

public class Rain {
    @SerializedName("1h")
    private float h1;

    public float getH1() {
        return h1;
    }
}
