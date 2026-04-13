package sgu.homework.bt2_weather_broadcast.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastItem {
    @SerializedName("dt")
    private long dt;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("dt_txt")
    private String dtTxt;

    public long getDt() { return dt; }
    public Main getMain() { return main; }
    public List<Weather> getWeather() { return weather; }
    public String getDtTxt() { return dtTxt; }
}
