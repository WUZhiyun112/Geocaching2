package com.example.geocaching1.model;

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
    // Geocache.java
// 修改日期格式常量以匹配API返回格式
    public static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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

    // Geocache.java
    public Geocache(String code, String name, String type, String location, String foundAtStr, String status) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = status;

        // 1. 解析location
        if (location != null) {
            String[] parts = location.split("\\|");
            try {
                this.latitude = new BigDecimal(parts[0]);
                this.longitude = new BigDecimal(parts[1]);
            } catch (Exception e) {
                Log.w("GeoCache", "Invalid location: " + location);
                this.latitude = this.longitude = BigDecimal.ZERO;
            }
        }

        // 2. 解析foundAt（关键修改）
        this.foundAt = parseFoundAt(foundAtStr);
        Log.d("GeoDebug", "Constructed: " + code + " | foundAtStr=" + foundAtStr + " | parsed=" + this.foundAt);
    }


    // Parcelable implementation
    protected Geocache(Parcel in) {
        code = in.readString();
        name = in.readString();

        // 1. 处理经纬度（保持不变）
        String latitudeStr = in.readString();
        String longitudeStr = in.readString();
        latitude = latitudeStr != null ? new BigDecimal(latitudeStr) : BigDecimal.ZERO;
        longitude = longitudeStr != null ? new BigDecimal(longitudeStr) : BigDecimal.ZERO;

        status = in.readString();
        type = in.readString();

        // 2. 关键修改：时间解析逻辑
        String foundAtString = in.readString();
        foundAt = parseFoundAt(foundAtString); // 使用统一解析方法
        Log.d("ParcelDebug", "Unparceled: " + code + " | rawDate=" + foundAtString + " | parsed=" + foundAt);

        // 3. 其他字段
        description = in.readString();
        size = in.readString();
        difficulty = in.readString();
        location = in.readString();
    }

    // 统一的日期解析方法（与构造函数共用）
    private Date parseFoundAt(String dateStr) {
        if (dateStr == null || dateStr.equalsIgnoreCase("N/A")) {
            return null;
        }

        // 优先尝试API格式（带T的ISO格式）
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(dateStr);
        } catch (ParseException e1) {
            // 尝试备用格式（不带T的格式）
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(dateStr);
            } catch (ParseException e2) {
                Log.w("DateParse", "Failed to parse: " + dateStr);
                return null;
            }
        }
    }
    public String getFormattedFoundAt() {
        if (foundAt == null) return "N/A";

        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return displayFormat.format(foundAt);  // 直接格式化Date对象
        } catch (Exception e) {
            return "N/A";
        }
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
        dest.writeString(latitude != null ? latitude.toString() : null);
        dest.writeString(longitude != null ? longitude.toString() : null);
        dest.writeString(status);
        dest.writeString(type);

        // 统一使用ISO格式写入
        dest.writeString(foundAt != null ?
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(foundAt) :
                null);

        dest.writeString(description);
        dest.writeString(size);
        dest.writeString(difficulty);
        dest.writeString(location);
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