package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV,humTV,rvTV;
    private TextInputEditText cityEdt;
    private RecyclerView weatherRV;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;
    private RequestQueue requestQueue;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        humTV=findViewById(R.id.idTVHum);
        homeRL=findViewById(R.id.idRLHome);
        loadingPB=findViewById(R.id.idPBLoading);
        cityNameTV=findViewById(R.id.idTVCityName);
        temperatureTV=findViewById(R.id.idTVTemperature);
        conditionTV=findViewById(R.id.idTVcondition);
        weatherRV=findViewById(R.id.idRvWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV =findViewById(R.id.idIVBack);
        iconIV=findViewById(R.id.idIVIcon);
        searchIV=findViewById(R.id.idIVSearch);
        rvTV=findViewById(R.id.idTVRv);
        weatherRVModalArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED ){
           ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);



        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);





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


    private void getWeatherInfo(String cityName){
        String url="https://api.weatherapi.com/v1/forecast.json?key=6edc74488de545e687630929221201 &q="+cityName+"&days=7&aqi=no&alerts=no";
        requestQueue.getCache().initialize();
        //requestQueue.start();

        JsonObjectRequest  jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                cityEdt.setText("");



                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                try {

                    String cityNameGet=response.getJSONObject("location").getString("name");
                        String temperature = response.getJSONObject("current").getString("temp_c");

                        temperatureTV.setText(temperature + "°C");
                        int isDay = response.getJSONObject("current").getInt("is_day");
                        String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                        String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                        Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                        conditionTV.setText(condition);
                        cityNameTV.setText(cityNameGet);

                        if (isDay == 1) {
                            Picasso.get().load("https://img1.baidu.com/it/u=3587364379,3049284261&fm=253&fmt=auto&app=138&f=JPEG?w=499&h=299").into(backIV);
                        } else {
                            Picasso.get().load("https://img1.baidu.com/it/u=3587364379,3049284261&fm=253&fmt=auto&app=138&f=JPEG?w=499&h=299").into(backIV);
                        }

                        JSONObject forecastobj = response.getJSONObject("forecast");
                        JSONObject forecastDay = forecastobj.getJSONArray("forecastday").getJSONObject(0);
                        JSONArray hourArray = forecastDay.getJSONArray("hour");
                        String forecast_date=forecastDay.getString("date");
                        rvTV.setText("Forecast for:"+forecast_date+"/in:"+cityNameGet);


                        for (int i = 0; i < hourArray.length(); i++) {
                            JSONObject hourobj = hourArray.getJSONObject(i);
                            String time = hourobj.getString("time");
                            String temper = hourobj.getString("temp_c");
                            String img = hourobj.getJSONObject("condition").getString("icon");
                            String wind = hourobj.getString("wind_kph");
                            String hum=hourobj.getString("humidity");
                            int day=hourobj.getInt("is_day");
                            int wind_value=hourobj.getInt("wind_kph");
                            int hum_value=hourobj.getInt("humidity");

                            if (day==1) {
                                int con=hourobj.getJSONObject("condition").getInt("code");
                                if ((con==1000||con==1003
                                )&&(wind_value<15)&&(hum_value<80)){
                                weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind, hum,0));
                                }else{
                                weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind, hum,1));
                                }
                            }
                            //double wind_value=Double.valueOf(wind);
                            //int hum_value=Integer.valueOf(hum);
                        }
                        weatherRVAdapter.notifyDataSetChanged();
                        requestQueue.stop();



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
