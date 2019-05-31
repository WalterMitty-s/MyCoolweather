package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.lang.ref.SoftReference;

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;
    public class Temperature{
        public String max;
        public String min;
    }

    @SerializedName("cond_txt_d")
    public String info;

}
