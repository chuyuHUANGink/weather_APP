package com.example.weatherapp;

public class WeatherRVModal {
    private String time;
    private String temperature;
    private String icon;
    private String windpseed;
    private String hum;
    private int con;



    public WeatherRVModal(String time, String temperature, String icon, String windpseed, String hum,int con) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.windpseed = windpseed;
        this.hum=hum;
        this.con=con;
    }

    public int getCon() {
        return con;
    }

    public void setCon(int con) {
        this.con = con;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getWindpseed() {
        return windpseed;
    }

    public void setWindpseed(String windpseed) {
        this.windpseed = windpseed;
    }

    public String getHum() { return hum; }

    public void setHum(String hum) { this.hum = hum; }


}
