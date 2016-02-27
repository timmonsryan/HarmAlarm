package com.hackathon.harmalarm;

import java.util.Date;

/**
 * Created by ryan.timmons on 2/27/16.
 */
public class TemperatureEntry {
    private int mTemperature;
    private String mDate;

    public TemperatureEntry(int temperature, String date) {
        mTemperature = temperature;
        mDate = date;
    }

    public int getTemperature() {
        return mTemperature;
    }

    public String getDate() {
        return mDate;
    }
}
