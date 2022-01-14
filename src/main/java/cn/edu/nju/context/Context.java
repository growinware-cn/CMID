package cn.edu.nju.context;

import java.util.Objects;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int id;
    private String timestamp;
    private String plateNumber;
    private double longitude;
    private double latitude;
    private double speed;
    private int status;

    public Context(int id, String timestamp, String plateNumber, double longitude, double latitude, double speed, int status) {
        this.id = id;
        this.timestamp = timestamp;
        this.plateNumber = plateNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String allForString() {
        return id + "," + timestamp + "," + plateNumber + ","
                + longitude + "," + latitude + "," + speed + "," + status;
    }

    @Override
    public String toString() {
        return "ctx_" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        return Objects.equals(plateNumber, context.plateNumber);
    }

    @Override
    public int hashCode() {
        return plateNumber != null ? plateNumber.hashCode() : 0;
    }
}
