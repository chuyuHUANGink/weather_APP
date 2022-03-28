package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    private RelativeLayout homeRL;
    private TextView cityNameTV,rvTV,alarmTV;
    private TextInputEditText cityEdt;
    private RecyclerView weatherRV;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1,day_counter=0;
    private String cityName;
    private RequestQueue requestQueue;
    private Switch filterSW;
    private ImageButton IBnext,IBback,IBre,IBAlarm;
    JSONObject responseGet;
    SimpleDateFormat input=new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private JSONObject forecastobj;
    private JSONObject forecastDay;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    public String clock_time="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar= Calendar.getInstance();
        createNotificationChannel();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        //Binding the variables with tools via tool's id.
        homeRL=findViewById(R.id.idRLHome);
        cityNameTV=findViewById(R.id.idTVCityName);
        weatherRV=findViewById(R.id.idRvWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV =findViewById(R.id.idIVBack);
        searchIV=findViewById(R.id.idIVSearch);
        rvTV=findViewById(R.id.idTVRv);
        weatherRVModalArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(handler,this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        filterSW=findViewById(R.id.idSWFilter);
        IBnext=findViewById(R.id.idIBNext);
        IBback=findViewById(R.id.idIbBack);
        IBre=findViewById(R.id.idIBRe);
        IBAlarm=findViewById(R.id.idIBAlarm);
        alarmTV=findViewById(R.id.idTVAlarm);

        //check the premission required automatically, and search for current city.
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED ){
           ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);


        //hide the card with bad weather condition, left the good and perfects.
        filterSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b==true){
                    ArrayList<WeatherRVModal> temp_list;
                    temp_list=new ArrayList<>();
                    for (WeatherRVModal modal:weatherRVModalArrayList){
                        if (modal.getCon()!=1){
                            temp_list.add(modal);
                        };}
                    WeatherRVAdapter temp_adapter=new WeatherRVAdapter(handler,MainActivity.this,temp_list);
                    weatherRV.setAdapter(temp_adapter);
                    if (temp_list.size()==0){
                        Toast.makeText(MainActivity.this,"No appropiate time find from:"+rvTV.getText(),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    weatherRV.setAdapter(weatherRVAdapter);
                }
            }
        }
        );

    //move to the information page of next day
    IBnext.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (day_counter<forecastDay.length()){
            FillweatherInfo(day_counter+1);
            if (weatherRVModalArrayList.size()==0){
                FillweatherInfo(day_counter);
                weatherRV.setAdapter(weatherRVAdapter);
                Toast.makeText(MainActivity.this,"already readch the maximum possible forecast!",Toast.LENGTH_SHORT).show();
            }else{
            IBre.setVisibility(View.VISIBLE);
            weatherRV.setAdapter(weatherRVAdapter);
            day_counter++;}
        }else{
         Toast.makeText(MainActivity.this,"already readch the maximum possible forecast!",Toast.LENGTH_SHORT).show();
            }
    }});

    //move to the information page of yesterday
    IBback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (day_counter!=0){
                day_counter=day_counter-1;
                FillweatherInfo(day_counter);
                if (day_counter==0){
                IBre.setVisibility(View.GONE);}
                weatherRV.setAdapter(weatherRVAdapter);}else{
                    Toast.makeText(MainActivity.this,"It's already today's forecast!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    //return to the information page of today's
    IBre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    day_counter=0;
                    FillweatherInfo(day_counter);
                    weatherRV.setAdapter(weatherRVAdapter);
                    IBre.setVisibility(View.GONE);
            }
        });

    //cancel alarm
    IBAlarm.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cancelAlarm();
        }
    });

    //find the weather information for certain city, if no entry, then search for default current city.
    searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"No entry, will find current city's information",Toast.LENGTH_SHORT).show();
                    cityName=getCityName(location.getLongitude(),location.getLatitude());
                    getWeatherInfo(cityName);
                }else{
                    getWeatherInfo(city);
                }
            }
        });
    }

    private void cancelAlarm() {
        Intent intent =  new Intent(this,AlarmReciver.class);

        pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);

        if (alarmManager==null){
             alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        }
        alarmManager.cancel(pendingIntent);
        Toast.makeText(MainActivity.this,"alarm canceled",Toast.LENGTH_SHORT).show();
        IBAlarm.setVisibility(View.INVISIBLE);
        alarmTV.setText("");
        alarmTV.setVisibility(View.INVISIBLE);



    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name="foxandriodRemindarChannel";
            String description ="Channel for Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel=new NotificationChannel("foxandroid",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this,"Permission granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,"pls provide the permissions",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    //Take the longtitude and latidtude as input then return the city name with this geo inputs.
    private String getCityName(double longitude, double latitude){
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address>addresses=gcd.getFromLocation(latitude,longitude,10);

            for (Address adr:addresses){
                if(adr!=null){
                    String city=adr.getLocality();
                    if(city!=null&&!city.equals("")){
                        cityName=city;
                    }else{
                        Log.d("TAG","City not found");
                        //Toast.makeText(this, "city not found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }


    public void FillweatherInfo(int day_index){
        try{
        weatherRVModalArrayList.clear();
        String cityNameGet=responseGet.getJSONObject("location").getString("name");
        forecastDay = forecastobj.getJSONArray("forecastday").getJSONObject(day_index);
        JSONArray hourArray = forecastDay.getJSONArray("hour");
        String forecast_date=forecastDay.getString("date");
        cityNameTV.setText(cityNameGet);
        rvTV.setText("Forecast for:"+forecast_date+"/in:"+cityNameGet);

        //process the weather information gathered from the json object.
        for (int i = 0; i < hourArray.length(); i++) {
            JSONObject hourobj = hourArray.getJSONObject(i);
            String time = hourobj.getString("time");
            String temper = hourobj.getString("temp_c");
            String img = hourobj.getJSONObject("condition").getString("icon");
            String wind = hourobj.getString("wind_mph");
            String hum=hourobj.getString("humidity");
            int day=hourobj.getInt("is_day");
            int wind_value=hourobj.getInt("wind_kph");
            int hum_value=hourobj.getInt("humidity");

        //A simple algorithm that rank hours into 3 level base on its weather condition.
            if (day==1){
                int con=hourobj.getJSONObject("condition").getInt("code");
                if ((con==1000||con==1003||con==1006)&&(wind_value<12)&&(hum_value<70)&&(wind_value>8)){
                    weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind, hum,0));
                }else{
                    if ((con==1000||con==1003||con==1006||con==1009)&&(wind_value<20)&&(hum_value<90)){
                    weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind, hum,2));
                    }else{
                    weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind, hum,1));}
                }
            }

        }
        weatherRVAdapter.notifyDataSetChanged();
        requestQueue.stop();
    } catch (JSONException e) {
        e.printStackTrace();
        requestQueue.stop();
    }
    }
    //send api calls to the website and get JSON as response.
    private void getWeatherInfo(String cityName){
        IBre.setVisibility(View.GONE);
        day_counter=0;
        String url="https://api.weatherapi.com/v1/forecast.json?key=6edc74488de545e687630929221201 &q="+cityName+"&days=7&aqi=no&alerts=no";
        requestQueue.getCache().initialize();
        JsonObjectRequest  jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                responseGet=response;
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                cityEdt.setText("");
                try {
                        forecastobj = responseGet.getJSONObject("forecast");
                        FillweatherInfo(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                    requestQueue.stop();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
                Toast.makeText(MainActivity.this, "pls enter valid city name", Toast.LENGTH_SHORT).show();
                requestQueue.stop();
            }
        });
        requestQueue.add(jsonObjectRequest);
        requestQueue.start();
    }

    private void setAlarm() {
        alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent =  new Intent(this,AlarmReciver.class);

        pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);

        Calendar calendar_temp = Calendar.getInstance();

        if (clock_time!="") {
            String[] s1=clock_time.split("-");
            int year=Integer.parseInt(s1[0]);
            int month=Integer.parseInt(s1[1])-1;
            String[] s2=s1[2].split(" ");
            int day=Integer.parseInt(s2[0]);
            String[] s3=s2[1].split(":");
            int hour=Integer.parseInt(s3[0]);


            // a simple condition checking to avoid a alrm on passed time made.
            try{ Date t=input.parse(clock_time);
                if (t.compareTo(calendar.getTime())>0){
                } else {
                    Toast.makeText(this,"sry, chosen time point already passed, pick another time", Toast.LENGTH_LONG).show();
                    return;}
            }catch(ParseException e){
                e.printStackTrace();
            }

            calendar_temp.set(calendar_temp.YEAR, year);
            calendar_temp.set(calendar_temp.MONTH, month);
            calendar_temp.set(calendar_temp.DAY_OF_MONTH, day);
            calendar_temp.set(calendar_temp.HOUR_OF_DAY, hour);
            calendar_temp.set(calendar_temp.MINUTE, 0);
            calendar_temp.set(calendar_temp.SECOND, 0);
            calendar_temp.set(calendar_temp.MILLISECOND, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar_temp.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar_temp.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

            }
            Toast.makeText(this,"Alarm set on"+calendar_temp.getTime().toString(), Toast.LENGTH_LONG).show();
            IBAlarm.setVisibility(View.VISIBLE);

        }

        else{
            Toast.makeText(this,"alarm setting failed".toString(),Toast.LENGTH_LONG).show();
        }
        alarmTV.setText("Current alarm:"+calendar_temp.getTime().toString());
        alarmTV.setVisibility(View.VISIBLE);
    clock_time="";
    }


    //used to receive information from card and alarm making.
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    clock_time=msg.getData().getString("NotificationTime");
                    setAlarm();
                default:
                    break;
            }
        }
    };



}
