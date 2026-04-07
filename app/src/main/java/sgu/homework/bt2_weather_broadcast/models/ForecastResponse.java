package sgu.homework.bt2_weather_broadcast.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    private List<ForecastItem> list;
    private City city;

    public List<ForecastItem> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }

    public static class City {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }


}
