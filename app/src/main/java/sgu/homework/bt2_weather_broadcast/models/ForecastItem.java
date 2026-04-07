package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastItem {
    @SerializedName("dt")
    private long dt; // Timestamp

    @SerializedName("main")
    private Main main; // Reuses your existing Main class

    @SerializedName("weather")
    private List<Weather> weather; // Reuses your existing Weather list

    @SerializedName("dt_txt")
    private String dtTxt; // Date-time string (e.g., "2023-10-25 15:00:00")

    public long getDt() {
        return dt;
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public String getDtTxt() {
        return dtTxt;
    }
}