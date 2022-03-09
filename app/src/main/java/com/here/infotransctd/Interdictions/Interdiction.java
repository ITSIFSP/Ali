package com.here.infotransctd.Interdictions;

import java.io.Serializable;

public class Interdiction implements Serializable
{
    private String description;
    private String organization;
    private String beginDate;
    private String endDate;
    private Local_Interdiction origin, destination;

    public Interdiction(String description, String organization, String beginDate,
                        String endDate, Local_Interdiction origin, Local_Interdiction destination)
    {
        this.description = description;
        this.organization = organization;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.origin = origin;
        this.destination = destination;
    }

    public Interdiction()
    {

    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getBeginDate()
    {
        return beginDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setBeginDate(String beginDate)
    {
        this.beginDate = beginDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public Local_Interdiction getOrigin()
    {
        return origin;
    }

    public void setOrigin(Local_Interdiction origin)
    {
        this.origin = origin;
    }

    public Local_Interdiction getDestination()
    {
        return destination;
    }

    public void setDestination(Local_Interdiction destination)
    {
        this.destination = destination;
    }
}
