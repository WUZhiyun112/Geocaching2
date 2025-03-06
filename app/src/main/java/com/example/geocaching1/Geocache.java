package com.example.geocaching1;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Geocache implements Parcelable {
    private String code;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private String type;
    private Date foundAt;

    // 日期格式
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public Geocache(String code, String name, BigDecimal latitude, BigDecimal longitude, String status, String type, Date foundAt) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.type = type;
        this.foundAt = foundAt;
    }

    // Parcelable implementation
    protected Geocache(Parcel in) {
        code = in.readString();
        name = in.readString();
        latitude = new BigDecimal(in.readString());
        longitude = new BigDecimal(in.readString());
        status = in.readString();
        type = in.readString();

        // 解析时间
        String foundAtString = in.readString();
        try {
            foundAt = foundAtString != null ? DATE_FORMATTER.parse(foundAtString) : null;
        } catch (ParseException e) {
            Log.e("Geocache", "Date parsing error: " + e.getMessage());
            foundAt = null;
        }
    }

    public static final Creator<Geocache> CREATOR = new Creator<Geocache>() {
        @Override
        public Geocache createFromParcel(Parcel in) {
            return new Geocache(in);
        }

        @Override
        public Geocache[] newArray(int size) {
            return new Geocache[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(name);
        dest.writeString(latitude.toString());
        dest.writeString(longitude.toString());
        dest.writeString(status);
        dest.writeString(type);

        // 写入时间
        dest.writeString(foundAt != null ? DATE_FORMATTER.format(foundAt) : null);
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public Date getFoundAt() {
        return foundAt;
    }

    public String getLocation() {
        return "Latitude: " + latitude + ", Longitude: " + longitude;
    }
}
