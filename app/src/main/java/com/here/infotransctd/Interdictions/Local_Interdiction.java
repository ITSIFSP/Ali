package com.here.infotransctd.Interdictions;

import java.io.Serializable;

public class Local_Interdiction implements Serializable
{
    String street, lat, lng;
    public Local_Interdiction(String street, String lat, String lng)
    {
        this.street = street;
        this.lat = lat;
        this.lng = lng;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public String getLat()
    {
        return lat;
    }

    public void setLat(String lat)
    {
        this.lat = lat;
    }

    public String getLng()
    {
        return lng;
    }

    public void setLng(String lng)
    {
        this.lng = lng;
    }
}
