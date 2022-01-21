package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;

    private TextView cityNameTV,temperatureTV,conditionTV,humTV,rvTV;
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
    private ImageButton IBnext,IBback,IBre;
    JSONObject responseGet;

    private JSONObject forecastobj;
    private JSONObject forecastDay;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        humTV=findViewById(R.id.idTVHum);
        homeRL=findViewById(R.id.idRLHome);
        cityNameTV=findViewById(R.id.idTVCityName);
        weatherRV=findViewById(R.id.idRvWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV =findViewById(R.id.idIVBack);
        searchIV=findViewById(R.id.idIVSearch);
        rvTV=findViewById(R.id.idTVRv);
        weatherRVModalArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        filterSW=findViewById(R.id.idSWFilter);
        IBnext=findViewById(R.id.idIBNext);
        IBback=findViewById(R.id.idIbBack);
        IBre=findViewById(R.id.idIBRe);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED ){
           ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

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
                    WeatherRVAdapter temp_adapter=new WeatherRVAdapter(MainActivity.this,temp_list);
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

    IBre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    day_counter=0;
                    FillweatherInfo(day_counter);
                    weatherRV.setAdapter(weatherRVAdapter);
                    IBre.setVisibility(View.GONE);
            }
        });



    searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"No entry, will find current city's information",Toast.LENGTH_SHORT).show();
                    cityName=getCityName(location.getLongitude(),location.getLatitude());
                    getWeatherInfo(cityName);
                }else{
                    //cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });
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

            if (day==1) {
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
}
