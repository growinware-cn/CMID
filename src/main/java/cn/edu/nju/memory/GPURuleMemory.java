package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


public class GPURuleMemory {

    private CUdeviceptr parent = new CUdeviceptr();

    private CUdeviceptr leftChild = new CUdeviceptr();

    private CUdeviceptr rightChild = new CUdeviceptr();

    private CUdeviceptr nodeType = new CUdeviceptr();

    private CUdeviceptr patternId = new CUdeviceptr();

    public GPURuleMemory(int length, int [] parent, int [] leftChild, int []rightChild, int [] nodeType, int [] patternId) {
        cuMemAlloc(this.parent, length * Sizeof.INT);
        cuMemcpyHtoD(this.parent, Pointer.to(parent), length * Sizeof.INT);

        cuMemAlloc(this.leftChild, length * Sizeof.INT);
        cuMemcpyHtoD(this.leftChild, Pointer.to(leftChild), length * Sizeof.INT);

        cuMemAlloc(this.rightChild, length * Sizeof.INT);
        cuMemcpyHtoD(this.rightChild, Pointer.to(rightChild), Sizeof.INT);

        cuMemAlloc(this.nodeType, length * Sizeof.INT);
        cuMemcpyHtoD(this.nodeType, Pointer.to(nodeType), length * Sizeof.INT);

        cuMemAlloc(this.patternId, length * Sizeof.INT);
        cuMemcpyHtoD(this.patternId, Pointer.to(patternId), length * Sizeof.INT);
    }

    public void free() {
        cuMemFree(this.parent);
        cuMemFree(this.leftChild);
        cuMemFree(this.rightChild);
        cuMemFree(this.nodeType);
    }

    public CUdeviceptr getParent() {
        return this.parent;
    }

    public CUdeviceptr getLeftChild() {
        return this.leftChild;
    }

    public CUdeviceptr getRightChild() {
        return this.rightChild;
    }

    public CUdeviceptr getNodeType() {
        return this.nodeType;
    }

    public CUdeviceptr getPatternId() {
        return this.patternId;
    }
}
