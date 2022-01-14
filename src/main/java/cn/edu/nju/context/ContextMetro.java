package cn.edu.nju.context;

public class ContextMetro {
    private int id;
    private String timestamp;
    private double DR_V;
    private double speed1;
    private double DR_I;
    private double speed2;
    private double DR_P;
    private String box_timestamp;
    private double Box_I;

    public ContextMetro(int id, String timestamp, double DR_V, double speed1, double DR_I, double speed2,
                   double DR_P,String box_timestamp,double Box_I) {
        this.id = id;
        this.timestamp = timestamp;
        this.DR_V = DR_V;
        this.speed1 = speed1;
        this.DR_I = DR_I;
        this.speed2 = speed2;
        this.DR_P = DR_P;
        this.box_timestamp = box_timestamp;
        this.Box_I = Box_I;
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

    public double getDR_V() {
        return DR_V;
    }

    public void setDR_V(double DR_V) {
        this.DR_V = DR_V;
    }

    public double getSpeed1() {
        return speed1;
    }

    public void setSpeed1(double speed1) {
        this.speed1 = speed1;
    }

    public double getDR_I() {
        return DR_I;
    }

    public void setDR_I(double DR_I) {
        this.DR_I = DR_I;
    }

    public double getSpeed2() {
        return speed2;
    }

    public void setSpeed2(double speed2) {
        this.speed2 = speed2;
    }

    public double getDR_P() {
        return DR_P;
    }

    public void setDR_P(double DR_P) {
        this.DR_P = DR_P;
    }

    public String getBox_timestamp() {
        return box_timestamp;
    }

    public void setBox_timestamp(String box_timestamp) {
        this.box_timestamp = box_timestamp;
    }

    public double getBox_I() {
        return Box_I;
    }

    public void setBox_I(double box_I) {
        Box_I = box_I;
    }

    public String allForString() {
        return id + "," + timestamp + "," + DR_V + ","
                + speed1 + "," + DR_I + "," + DR_P + "," + box_timestamp+ "," + Box_I;
    }
    @Override
    public String toString() {
        return "ctx_" + id;
    }
}
