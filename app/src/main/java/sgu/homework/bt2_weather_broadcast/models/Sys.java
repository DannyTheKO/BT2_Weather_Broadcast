package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

public class Sys {
    @SerializedName("country")
    private String country;

    @SerializedName("sunrise")
    private Long sunrise;

    @SerializedName("sunset")
    private Long sunset;

    public String getCountry() { return country; }

    public Long getSunrise() { return sunrise; }

    public Long getSunset() { return sunset; }
}
