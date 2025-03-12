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
    private String description;  // 新增字段
    private String size;         // 新增字段
    private String difficulty;   // 新增字段
    private String location;     // 新增字段

    // 日期格式
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // 更新构造函数，新增字段
    public Geocache(String code, String name, BigDecimal latitude, BigDecimal longitude, String status, String type, Date foundAt, String description, String size, String difficulty) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.type = type;
        this.foundAt = foundAt;
        this.description = description;
        this.size = size;
        this.difficulty = difficulty;
        this.location = latitude + "|" + longitude;  // 计算并设置location
    }

    public Geocache(String code, String name, String difficulty) {
        this.code = code;
        this.name = name;
        this.difficulty = difficulty;
    }

    // 新增构造函数，支持通过location构造
    public Geocache(String code, String name, String type, String location ) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.location = location;

        // 初始化 latitude 和 longitude
        this.latitude = BigDecimal.ZERO;
        this.longitude = BigDecimal.ZERO;

        // 解析 location 获取 latitude 和 longitude
        if (location != null && !location.isEmpty()) {
            String[] locationParts = location.split("\\|");
            if (locationParts.length == 2) {
                try {
                    this.latitude = new BigDecimal(locationParts[0]);
                    this.longitude = new BigDecimal(locationParts[1]);
                } catch (NumberFormatException e) {
                    Log.e("Geocache", "Location parsing error: " + e.getMessage());
                    // 保持默认值 BigDecimal.ZERO
                }
            }
        }
    }
    // Parcelable implementation
    protected Geocache(Parcel in) {
        code = in.readString();
        name = in.readString();

        // 处理 latitude 和 longitude 的空值
        String latitudeStr = in.readString();
        String longitudeStr = in.readString();
        latitude = latitudeStr != null ? new BigDecimal(latitudeStr) : BigDecimal.ZERO;
        longitude = longitudeStr != null ? new BigDecimal(longitudeStr) : BigDecimal.ZERO;

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

        // 读取新增字段
        description = in.readString();
        size = in.readString();
        difficulty = in.readString();
        location = in.readString();  // 读取location
    }
    @Override
    public String toString() {
        return "Geocache{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", foundAt=" + (foundAt != null ? DATE_FORMATTER.format(foundAt) : "null") +
                ", description='" + description + '\'' +
                ", size='" + size + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", location='" + location + '\'' +
                '}';
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
        dest.writeString(latitude != null ? latitude.toString() : "0");
        dest.writeString(longitude != null ? longitude.toString() : "0");
        dest.writeString(status);
        dest.writeString(type);

        // 写入时间
        dest.writeString(foundAt != null ? DATE_FORMATTER.format(foundAt) : null);

        // 写入新增字段
        dest.writeString(description);
        dest.writeString(size);
        dest.writeString(difficulty);
        dest.writeString(location);  // 写入location
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

    public String getDescription() {
        return description;  // Getter for description
    }

    public String getSize() {
        return size;  // Getter for size
    }

    public String getDifficulty() {
        return difficulty;  // Getter for difficulty
    }

    public String getLocation() {
        return location;  // 返回 location
    }

    public String getGeocacheCode() {
        return code;
    }


}