package sgu.homework.bt2_weather_broadcast.data.models;

import com.google.gson.annotations.SerializedName;

public class Sys {
    @SerializedName("country")
    private String country;
    @SerializedName("sunrise")
    private long sunrise;
    @SerializedName("sunset")
    private long sunset;

    public String getCountry() { return country; }
    public long getSunrise() { return sunrise; }
    public long getSunset() { return sunset; }
}
