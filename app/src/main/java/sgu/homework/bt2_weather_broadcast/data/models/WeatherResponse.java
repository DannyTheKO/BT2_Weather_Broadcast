package sgu.homework.bt2_weather_broadcast.data.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("weather")
    private List<Weather> weather;
    @SerializedName("main")
    private Main main;
    @SerializedName("wind")
    private Wind wind;
    @SerializedName("rain")
    private Rain rain;
    @SerializedName("sys")
    private Sys sys;
    @SerializedName("name")
    private String cityName;

    public List<Weather> getWeather() { return weather; }
    public Main getMain() { return main; }
    public Wind getWind() { return wind; }
    public Rain getRain() { return rain; }
    public Sys getSys() { return sys; }
    public String getCityName() { return cityName; }
}
