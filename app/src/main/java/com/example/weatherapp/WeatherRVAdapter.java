package com.example.weatherapp;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    public Handler handler;



    public WeatherRVAdapter(Handler handler,Context context, ArrayList<WeatherRVModal> weatherRVModalArrayList) {
        this.context = context;
        this.weatherRVModalArrayList = weatherRVModalArrayList;
        this.handler=handler;
    }

    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
        return new ViewHolder(view);
    }




    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {

        WeatherRVModal modal=weatherRVModalArrayList.get(position);
        holder.temperatureTV.setText(modal.getTemperature()+"°C");
        holder.windTV.setText(modal.getWindpseed()+"mile/h");
        holder.humTV.setText("humidity:"+modal.getHum());


        //send tiem information to main activity with choosen card for alarm use.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg=new Message();
                Bundle bundle=new Bundle();
                bundle.putString("NotificationTime", modal.getTime());
                msg.setData(bundle);
                msg.what=1;
                handler.sendMessage(msg);

            }
        });





        //change the color of the card show the weather condition rank base on algorithm from main activity.
        if (modal.getCon()==0){
        holder.itemView.setBackgroundColor(Color.YELLOW);}
        if (modal.getCon()==1){
            holder.itemView.setBackgroundColor(Color.RED);
        }
        if (modal.getCon()==2){
            holder.itemView.setBackgroundColor(Color.GREEN);
        }







        Picasso.get().load("http:".concat(modal.getIcon())).into(holder.conditioniv);
        SimpleDateFormat input=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output=new SimpleDateFormat("hh:mm aa");
        try{
            Date t=input.parse(modal.getTime());
            holder.timeTV.setText(output.format(t));
        }catch(ParseException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return weatherRVModalArrayList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView windTV,temperatureTV,timeTV,humTV;
        private ImageView conditioniv;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTV =itemView.findViewById(R.id.idTVWindspeed);
            temperatureTV =itemView.findViewById(R.id.idTVTemperature);
            timeTV =itemView.findViewById(R.id.idTVTime);
            conditioniv =itemView.findViewById(R.id.idIVCondition);
            humTV=itemView.findViewById(R.id.idTVHum);





        }
    }
}
