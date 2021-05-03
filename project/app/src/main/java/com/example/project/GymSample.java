package com.example.project;

public class GymSample {
    private String name;
    private double latitude;
    private double longitude;
    private  String address;
    private String state;
    private String number;

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getNumber() { return number; }

    public void setNumber(String number) { this.number = number; }

    public String getState() { return state; }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return name +"\n"+ address;
    }
}
