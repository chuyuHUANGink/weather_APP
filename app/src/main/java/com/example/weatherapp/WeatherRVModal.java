package com.example.weatherapp;

public class WeatherRVModal {
    private String time;
    private String temperature;
    private String icon;
    private String windpseed;

    public WeatherRVModal(String time, String temperature, String icon, String windpseed) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.windpseed = windpseed;
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


}
