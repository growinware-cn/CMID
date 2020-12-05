package cn.edu.nju.util;

/**
 * Created by njucjc on 2017/10/7.
 */
public class LocationHelper {


    public static double getDistance(double long1, double lat1, double long2, double lat2) {
       return Math.sqrt((long1 - long2) * (long1 - long2) + (lat1 - lat2) * (lat1 - lat2));
    }

    public static void main(String[] args) {
        //根据两点间的经纬度计算距离，单位：km
        System.out.println(getDistance(116.5542, 39.81621, 116.5539, 39.81616));
        System.out.println(getDistance(117.1181, 36.68484, 117.01, 36.66123));
        System.out.println(getDistance(112.9084, 28.14203, 112.9083, 28.14194));
        System.out.println(getDistance(121.5373, 38.86827, 121.5372, 38.86832));
        System.out.println(getDistance(20.5, 118.2, 21.1, 117.6));
        System.out.println(getDistance(121.445140,31.177779, 121.444832,31.179313));
    }
}
