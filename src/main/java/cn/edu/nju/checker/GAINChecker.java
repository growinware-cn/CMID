package cn.edu.nju.checker;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.memory.*;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.NodeType;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LogFileHelper;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdeviceptr;
import jcuda.runtime.dim3;
import jcuda.utils.KernelLauncher;

import java.util.*;

public class GAINChecker extends Checker {

    private int stSize;

    private int [] branchSize;

    private STNode [] constraintNodes; //(an array for storing a reordered syntax tree)

    private List<Integer> cunits; //an array for storing the start of each c-unit

    private GPURuleMemory gpuRuleMemory;

    //private KernelLauncher genTruthValue;

   // private KernelLauncher genLinks;

    private  KernelLauncher evaluation;

    private GPUContextMemory gpuContextMemory;

    private GPUPatternMemory gpuPatternMemory;

    private Map<String, Integer> patternIdMap;

    private static final int threadPerBlock = 512;

    private CUdeviceptr deviceTruthValueResult;

    private CUdeviceptr deviceBranchSize = new CUdeviceptr();

    private CUdeviceptr deviceLinkResult;

    private CUdeviceptr deviceLinkNum;

    private CUdeviceptr deviceMaxLinkSize;

    private CUcontext cuContext;


    public GAINChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap,
                       String kernelFilePath,
                       List<String> contexts, CUcontext cuContext, GPUResult gpuResult) {
        super(name, stRoot, patternMap, stMap);
        init(kernelFilePath, contexts, cuContext, gpuResult);
    }

    public GAINChecker(Checker checker, String kernelFilePath, List<String> contexts, CUcontext cuContext, GPUResult gpuResult){
        super(checker);
        init(kernelFilePath, contexts, cuContext, gpuResult);
    }

    private void init(String kernelFilePath, List<String> contexts, CUcontext cuContext, GPUResult gpuResult) {
        this.stSize = computeSTSize(this.stRoot);
        //       System.out.println(name + ": " + stSize);

        this.branchSize = new int[this.stSize];

        //计算cunit以及为语法树重排序(前序遍历)
        this.constraintNodes = new STNode[this.stSize];
        this.cunits = new ArrayList<>();
        split(this.stRoot);
        this.cunits.add(-1);
        //将语法树信息拷贝到GPU

        //this.genTruthValue = KernelLauncher.load(kernelFilePath, "gen_truth_value");
        //this.genLinks = KernelLauncher.load(kernelFilePath, "gen_links");
        this.evaluation = KernelLauncher.load(kernelFilePath, "evaluation");

        this.gpuContextMemory = GPUContextMemory.getInstance(contexts);
        this.gpuPatternMemory = new GPUPatternMemory(this.stMap.keySet());

        this.patternIdMap = gpuPatternMemory.getIndexMap();

        initGPURuleMemory(gpuResult);
        this.cuContext = cuContext;
    }

    private void updateGPUPatternMemory() {
        int size = 0;
        for(String key : stMap.keySet()) {
            size += patternMap.get(key).getContextList().size();
        }



        Map<Integer, String> nameMap = gpuPatternMemory.getNameMap();
        int curBegin = 0;
        int [] contexts = new int[size];
        int [] begin = new int[nameMap.keySet().size()];
        int [] length = new int[nameMap.keySet().size()];
        for(int i = 0; i < nameMap.keySet().size();i++) {
            List<Context> contextList = patternMap.get(nameMap.get(i)).getContextList();
            length[i] = contextList.size();
            begin[i] = curBegin;
            for(int j = curBegin; j < curBegin + length[i]; j++) {
                contexts[j] = contextList.get(j - curBegin).getId();
            }
            curBegin = curBegin + length[i];
        }

        gpuPatternMemory.update(begin, length, contexts);
    }

    private void initGPURuleMemory(GPUResult gpuResult) {

        this.deviceTruthValueResult = gpuResult.getDeviceTruthValueResult();
        this.deviceLinkResult = gpuResult.getDeviceLinkResult();
        this.deviceLinkNum = gpuResult.getDeviceLinkNum();
        this.deviceMaxLinkSize = gpuResult.getDeviceMaxLinkSize();

        cuMemAlloc(this.deviceBranchSize, stSize * Sizeof.INT);


        int [] parent = new int[stSize];
        int [] leftChild = new int[stSize];
        int [] rightChild = new int[stSize];
        int [] nodeType = new int[stSize];
        int [] patternId = new int[stSize];

        for(int i = 0; i < stSize; i++) {
            STNode p = (STNode) constraintNodes[i].getParentTreeNode();
            STNode l = (STNode) constraintNodes[i].getFirstChild();
            STNode r = (STNode) constraintNodes[i].getLastChild();

            parent[i] = p != null ? p.getNodeNum() : -1;
            leftChild[i] = l != null ? l.getNodeNum() : -1;
            rightChild[i] = r != null ? r.getNodeNum() : -1;
            patternId[i] = -1;

            int type = constraintNodes[i].getNodeType();
            if(type != NodeType.BFUNC_NODE) {
                nodeType[i] = type;
                if(type == NodeType.UNIVERSAL_NODE || type == NodeType.EXISTENTIAL_NODE) {
                    patternId[i] = patternIdMap.get(constraintNodes[i].getContextSetName());
                }
            }
            else {
                String name = constraintNodes[i].getNodeName();
                if("sz_loc_range".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_RANGE;
                }
                else if("same".equals(name)) {
                    nodeType[i] = NodeType.SAME;
                }
                else if("sz_loc_close".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_CLOSE;
                }
                else if("sz_spd_close".equals(name)) {
                    nodeType[i] = NodeType.SZ_SPD_CLOSE;
                }
                else if("sz_loc_dist".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_DIST;
                }
                else if("sz_loc_dist_neq".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_DIST_NEQ;
                }
                else {
                    assert false:"BFunc type error.";
                }
            }

            this.gpuRuleMemory = new GPURuleMemory(stSize, parent, leftChild, rightChild, nodeType, patternId);
        }
    }



    @Override
    public boolean doCheck() {
       // assert false:"Something is being to do.";
        checkTimes++;
        computeRTTBranchSize(this.stRoot);
        int cctSize = branchSize[stSize - 1];

        assert cctSize <= Config.MAX_CCT_SIZE:"CCT size overflow: " + cctSize;

        cuCtxPushCurrent(cuContext);

        updateGPUPatternMemory();
        cuMemcpyHtoD(this.deviceBranchSize, Pointer.to(branchSize), stSize * Sizeof.INT);

        for(int i = cunits.size() - 2; i >= 0; i--) {
            int ccopyNum = computeCCopyNum(cunits.get(i));
            //System.out.println("num: " + ccopyNum);
            if(ccopyNum == 0) {
                continue;
            }

            dim3 gridSize = new dim3(threadPerBlock, 1, 1);
            dim3 blockSize = new dim3((ccopyNum + threadPerBlock - 1) / threadPerBlock,1, 1);

            //gen truth value and links
            evaluation.setup(gridSize, blockSize)
                    .call(gpuRuleMemory.getParent(), gpuRuleMemory.getLeftChild(), gpuRuleMemory.getRightChild(), gpuRuleMemory.getNodeType(), gpuRuleMemory.getPatternId(),
                            deviceBranchSize, cunits.get(i + 1) + 1, cunits.get(i),
                            gpuPatternMemory.getBegin(), gpuPatternMemory.getLength(), gpuPatternMemory.getContexts(),
                            gpuContextMemory.getLongitude(), gpuContextMemory.getLatitude(), gpuContextMemory.getSpeed(), gpuContextMemory.getPlateNumber(),
                            deviceTruthValueResult,
                            deviceLinkResult, deviceLinkNum, deviceMaxLinkSize,
                            cunits.get(0),
                            ccopyNum);

        }

        short [] hostTruthValueResult = new short[1];
        cuMemcpyDtoH(Pointer.to(hostTruthValueResult), deviceTruthValueResult, Sizeof.SHORT);

        boolean value = hostTruthValueResult[0] == 1;
//        System.out.println(Arrays.toString(hostTruthValue));


        if(!value) {
            int [] hostLinkNum = new int[1];
            cuMemcpyDtoH(Pointer.to(hostLinkNum), deviceLinkNum, Sizeof.INT);

            int [] hostMaxLinkSize = new int[1];
            cuMemcpyDtoH(Pointer.to(hostMaxLinkSize), deviceMaxLinkSize, Sizeof.INT);

            this.maxLinkSize = this.maxLinkSize > hostMaxLinkSize[0] ? this.maxLinkSize : hostMaxLinkSize[0];

            int [] hostLinkResult = new int[Config.MAX_PARAN_NUM * Config.MAX_LINK_SIZE];
            cuMemcpyDtoH(Pointer.to(hostLinkResult), deviceLinkResult, (Config.MAX_PARAN_NUM * hostLinkNum[0]) * Sizeof.INT);
            //           System.out.println(Arrays.toString(hostLinkResult));
            parseLink(hostLinkResult, hostLinkNum[0]);
        }


        cuCtxPopCurrent(cuContext);


        return value;
    }

    private void parseLink(int [] links, int size) {

        clearCriticalSet();

        for(int i = 0; i < size; i++) {
            String link = "";
            for(int j = 0; j < Config.MAX_PARAN_NUM; j++) {
                if(links[i * Config.MAX_PARAN_NUM + j] != -1) {
                    link = link + "ctx_" + links[i * Config.MAX_PARAN_NUM + j] + " ";
                }
            }

            addCriticalSet(link);

            if(addIncLink(link)) {
                LogFileHelper.getLogger().info(getName() + " " + link, false);
            }
            //link = link.substring(0, link.length() - 1);
        }
    }

    @Override
    public boolean add(String patternId, Context context) {
      //  return super.add(patternId, context);
        if (!addContextToPattern(patternId, context)) {
            return false;
        }
        assert patternMap.get(patternId).getContextList().size() <= Config.MAX_PATTERN_SIZE:"pattern size overflow.";
        return true;
    }



    @Override
    public boolean delete(String patternId, String timestamp) {
        if(!deleteContextFromPattern(patternId, timestamp)) {
            return false;
        }
        assert patternMap.get(patternId).getContextList().size() <= Config.MAX_PATTERN_SIZE:"pattern size overflow.";
        return true;
    }


    private int computeSTSize(STNode root) {
        if(root == null) {
            return 0;
        }
        int type = root.getNodeType();
        if(type == STNode.UNIVERSAL_NODE || type == NodeType.EXISTENTIAL_NODE || type == STNode.NOT_NODE) {
            return 1 + computeSTSize((STNode) root.getFirstChild());
        }
        else if(type == STNode.AND_NODE || type == STNode.IMPLIES_NODE) { //not support 'OR' node type
            return 1 + computeSTSize((STNode) root.getFirstChild()) + computeSTSize((STNode) root.getLastChild());
        }
        else if(type == STNode.BFUNC_NODE) {
            return 1;
        }
        else {
            assert false:"Node type error, type:  " +  type;
            return 0;
        }

    }

    private void split(STNode root) {
        Queue<STNode> rootOfCunit = new LinkedList<>();
        rootOfCunit.offer(root);
        int [] currentNodeNum = new int[1];
        currentNodeNum[0] = this.stSize - 1;
        while(!rootOfCunit.isEmpty()) {
            STNode rootOfNextCunit = rootOfCunit.poll();
            cunits.add(currentNodeNum[0]);
            parseCunit(rootOfCunit, rootOfNextCunit, currentNodeNum);
        }
    }

    private void parseCunit(Queue<STNode> rootOfCunit, STNode node,int []currentNodeNum) {
        //假定非全称/存在量词的子树中不会有全称/存在量词节点
        if(node == null) {
            return ;
        }
  //      System.out.println(currentNodeNum[0] + " :" + node.getNodeName());
        node.setNodeNum(currentNodeNum[0]);
        constraintNodes[currentNodeNum[0]] = node;
        currentNodeNum[0]--;
        int type = node.getNodeType();
        if (type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
            rootOfCunit.offer((STNode) node.getFirstChild());
        }
        else if(type == STNode.IMPLIES_NODE || type == STNode.AND_NODE){
            parseCunit(rootOfCunit, (STNode) node.getLastChild(), currentNodeNum);
            parseCunit(rootOfCunit, (STNode) node.getFirstChild(), currentNodeNum);
        }
        else if(type == STNode.NOT_NODE) {
            parseCunit(rootOfCunit, (STNode) node.getFirstChild(), currentNodeNum);
        }
        else if(type == STNode.BFUNC_NODE) {
            return;
        }
    }

    private int computeCCopyNum(int cunit) {
        STNode node = (STNode)constraintNodes[cunit].getParentTreeNode();
        int ccopyNum = 1;
        while(node != null) {
            int type = node.getNodeType();
            if(type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
                ccopyNum *= patternMap.get(node.getContextSetName()).getContextList().size();
            }
            node = (STNode) node.getParentTreeNode();
        }
        return ccopyNum;
    }

    private int computeRTTBranchSize(STNode root) {
        assert root != null:"root is null.";
        int type = root.getNodeType();
        int size = 0;
        if(type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
            size = 1 + patternMap.get(root.getContextSetName()).getContextList().size() * computeRTTBranchSize((STNode) root.getFirstChild());
        }
        else if(type == STNode.NOT_NODE) {
            size = 1 + computeRTTBranchSize((STNode)root.getFirstChild());
        }
        else if(type == STNode.AND_NODE || type == STNode.IMPLIES_NODE) {
            size = 1 + computeRTTBranchSize((STNode)root.getFirstChild()) + computeRTTBranchSize((STNode)root.getLastChild());
        }
        else if(type == STNode.BFUNC_NODE){
            size = 1;
        }
        else {
            assert false:"Type error.";
        }
        branchSize[root.getNodeNum()] = size;
        return size;
    }

    @Override
    public void reset() {
        cuMemFree(this.deviceBranchSize);
        this.gpuPatternMemory.free();
        super.reset();
    }

    @Override
    public void sCheck(List<Context> contextList) {
        CCTNode newRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        build(stRoot, newRoot, 2);

        List<Context> param = new ArrayList<>();
        evaluation(newRoot, param);
    }
}