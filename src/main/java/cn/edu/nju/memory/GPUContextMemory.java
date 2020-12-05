package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


import java.util.*;

public class GPUContextMemory {

    private CUdeviceptr longitude = new CUdeviceptr();

    private CUdeviceptr latitude = new CUdeviceptr();

    private CUdeviceptr speed = new CUdeviceptr();

    private CUdeviceptr plateNumber = new CUdeviceptr();


    private static GPUContextMemory gpuContextMemory;

    private GPUContextMemory(List<String> contextStrList) {
        int size = contextStrList.size();

        ContextParser parser = new ContextParser();
        double [] longitudeRaw = new double[size];
        double [] latitudeRaw = new double[size];
        double [] speedRaw = new double[size];

        int [] plateNumberRaw = new int[size];

        for(int i = 0; i < size; i++) {
            Context c = parser.parseContext(i,contextStrList.get(i));
            longitudeRaw[i] = c.getLongitude();
            latitudeRaw[i] = c.getLatitude();
            speedRaw[i] = c.getSpeed();
            plateNumberRaw[i] = parseInt(c.getPlateNumber());//Integer.parseInt(c.getPlateNumber());
        }

        cuMemAlloc(this.longitude, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.longitude, Pointer.to(longitudeRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.latitude, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.latitude, Pointer.to(latitudeRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.speed, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.speed, Pointer.to(speedRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.plateNumber, size * Sizeof.INT);
        cuMemcpyHtoD(this.plateNumber, Pointer.to(plateNumberRaw), size * Sizeof.INT);

    }

    public static synchronized GPUContextMemory getInstance(List<String> contextStrList) {
        if(gpuContextMemory == null) {
            gpuContextMemory = new GPUContextMemory(contextStrList);
        }
        return gpuContextMemory;
    }

    public synchronized void free() {
//        cuMemFree(this.latitude);
//        cuMemFree(this.longitude);
//        cuMemFree(this.speed);
//        cuMemFree(this.plateNumber);
    }

    private int parseInt(String str) {
        int res = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(c >= '0' && c <= '9') {
                res = (res + (c - '0')) * 36;
            }
            else {
                res = (res + (c - 'A' + 10)) * 36;
            }
        }
        return res;
    }

    public CUdeviceptr getLongitude() {
        return longitude;
    }

    public CUdeviceptr getLatitude() {
        return latitude;
    }

    public CUdeviceptr getSpeed() {
        return speed;
    }

    public CUdeviceptr getPlateNumber() {
        return plateNumber;
    }
}
