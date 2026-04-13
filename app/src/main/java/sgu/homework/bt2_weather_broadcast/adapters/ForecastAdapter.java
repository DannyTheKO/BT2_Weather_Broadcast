package sgu.homework.bt2_weather_broadcast.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import sgu.homework.bt2_weather_broadcast.R;
import sgu.homework.bt2_weather_broadcast.data.models.ForecastItem;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    public void setForecastList(List<ForecastItem> forecastList) {
        this.forecastList = forecastList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);
        
        // Time from dt_txt (Format: "2023-10-25 15:00:00")
        // Simple substring to get the HH:mm
        String timeStr = item.getDtTxt();
        if (timeStr != null && timeStr.length() >= 16) {
            holder.tvTime.setText(timeStr.substring(11, 16));
        }

        holder.tvTemp.setText(Math.round(item.getMain().getTemp()) + "°C");

        if (item.getWeather() != null && !item.getWeather().isEmpty()) {
            String iconCode = item.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .into(holder.ivWeatherIcon);
        }
    }

    @Override
    public int getItemCount() {
        return forecastList != null ? forecastList.size() : 0;
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView ivWeatherIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
}
