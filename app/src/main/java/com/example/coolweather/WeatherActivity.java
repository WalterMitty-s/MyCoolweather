package com.example.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;//basic location
    private TextView titleUpdateTime;//update time
    private TextView degreeText;//now temperature
    private TextView weatherInfoText;// now condition
//    private LinearLayout forecastLayout;//forecast date,tem,max,min
////    private TextView aqiText;//useless
////    private TextView pm25Text;//useless
//    private TextView comfortText;//lifestyle comfort
//    private TextView suggestionText;//lifestyle suggestion
//    //private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;//滑动菜单
    private Button navButton;//按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout=(ScrollView) findViewById(R.id.weather_layout);
        titleCity=(TextView) findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
//        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
//        aqiText=(TextView)findViewById(R.id.aqi_text);
//        pm25Text=(TextView)findViewById(R.id.pm25_text);
//        comfortText=(TextView)findViewById(R.id.comfort_text);
//        suggestionText=(TextView)findViewById(R.id.suggestion_text);
        //sportText=(TextView)findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);//每日一图
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh); //下拉刷新
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//设置颜色

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);

        final String weatherId;//设置变量
        if(weatherString!=null){
            //Log.d("weatherString",weatherString);
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.cid;
            showWeatherInfo(weather);
        }else{
            weatherId=getIntent().getStringExtra("weather_id");
            //String weatherId=getIntent().getStringExtra("countyName");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        //刷新天气
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        String bingPic=prefs.getString("bing_pic",null);
        //每日一图
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    public void requestWeather(final String weatherId){
        String weatherUrl="https://free-api.heweather.com/s6/weather/now?key=e1984444a03d4a52a1f6cc545cce9245&location="+weatherId;
        //this.weatherId=weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);

                    }
                });

            }
        });
        loadBingPic();
    }

    //加载每日一图
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });

    }

    private void showWeatherInfo (Weather weather){
        String cityName=weather.basic.locationName;
        String updateTime=weather.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.cond_txt;;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
//        forecastLayout.removeAllViews();
//        for(Forecast forecast:weather.forecastList){
//            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
//            TextView dateText=(TextView)findViewById(R.id.date_text);
//            TextView infoText=(TextView)findViewById(R.id.info_text);
//            TextView maxText=(TextView)findViewById(R.id.max_text);
//            TextView minText=(TextView)findViewById(R.id.min_text);
//            dateText.setText(forecast.date);
//            infoText.setText(forecast.info);
//            maxText.setText(forecast.temperature.max);
//            minText.setText(forecast.temperature.min);
//            forecastLayout.addView(view);
//        }
//        if(weather.aqi!=null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
//        String comfort="舒适度:"+ weather.lifeStyle.comfort;
//        String carWash="建议:"+ weather.lifeStyle.suggestion;
        //String sport="舒适度:"+ weather.suggestion.sport.info;
//        comfortText.setText(comfort);
//        suggestionText.setText(carWash);
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
