package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GPUPatternMemory {

    private CUdeviceptr begin = new CUdeviceptr();

    private CUdeviceptr length = new CUdeviceptr();

    private CUdeviceptr contexts = new CUdeviceptr();

    private Map<String, Integer> indexMap = new ConcurrentHashMap<>();

    private Map<Integer, String> nameMap = new ConcurrentHashMap<>();

    int patternNum;

    public GPUPatternMemory(Set<String> keySet) {
        int index = 0;
        for(String key : keySet) {
            indexMap.put(key, index);
            nameMap.put(index, key);
            index++;
        }

        this.patternNum = keySet.size();
        cuMemAlloc(this.begin, patternNum * Sizeof.INT);

        cuMemAlloc(this.length, patternNum * Sizeof.INT);

        cuMemAlloc(this.contexts, patternNum * Config.MAX_PATTERN_SIZE * Sizeof.INT);
    }

    public CUdeviceptr getBegin() {
        return this.begin;
    }

    public CUdeviceptr getLength() {
        return this.length;
    }

    public CUdeviceptr getContexts() {
        return this.contexts;
    }

    public Map<String, Integer> getIndexMap() {
        return this.indexMap;
    }

    public Map<Integer, String> getNameMap() {
        return nameMap;
    }

    public void update(int [] begin, int [] length, int [] contexts) {
        cuMemcpyHtoD(this.begin, Pointer.to(begin), this.patternNum * Sizeof.INT);
        cuMemcpyHtoD(this.length, Pointer.to(length),patternNum * Sizeof.INT );

        assert contexts.length <= patternNum * Config.MAX_PATTERN_SIZE:"size overflow.";
        cuMemcpyHtoD(this.contexts, Pointer.to(contexts), contexts.length * Sizeof.INT);
    }

    public synchronized void free() {
        cuMemFree(this.begin);
        cuMemFree(this.length);
        cuMemFree(this.contexts);
    }

}
