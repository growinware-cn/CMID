package cn.edu.nju.memory;

import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemFree;

public class GPUResult {

    private CUdeviceptr deviceTruthValueResult = new CUdeviceptr();

    private CUdeviceptr deviceLinkResult = new CUdeviceptr();

    private CUdeviceptr deviceLinkNum = new CUdeviceptr();

    private CUdeviceptr deviceMaxLinkSize = new CUdeviceptr();


    public GPUResult() {
        cuMemAlloc(this.deviceTruthValueResult, Sizeof.SHORT);
        cuMemAlloc(this.deviceLinkResult, (Config.MAX_PARAN_NUM * Config.MAX_LINK_SIZE) * Sizeof.INT);
        cuMemAlloc(this.deviceLinkNum, Sizeof.INT);
        cuMemAlloc(this.deviceMaxLinkSize, Sizeof.INT);
    }
    public CUdeviceptr getDeviceTruthValueResult() {
        return deviceTruthValueResult;
    }

    public CUdeviceptr getDeviceLinkResult() {
        return deviceLinkResult;
    }

    public CUdeviceptr getDeviceLinkNum() {
        return deviceLinkNum;
    }

    public CUdeviceptr getDeviceMaxLinkSize() {
        return deviceMaxLinkSize;
    }

    public synchronized void free() {
//        cuMemFree(this.deviceTruthValue);
//        cuMemFree(this.deviceTruthValueResult);
//        cuMemFree(this.deviceLinks);
//        cuMemFree(this.deviceLinkResult);
//        cuMemFree(this.deviceLinkNum);
    }
}
