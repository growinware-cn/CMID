package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.NodeStatus;
import cn.edu.nju.node.NodeType;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ConPccChecker extends PccChecker {
    private Checker pcc;

    private int addNum;

    private int taskNum;

    private ExecutorService checkExecutorService;


    public ConPccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum, ExecutorService checkExecutorService) {
        super(name, stRoot, patternMap, stMap);
        this.pcc = new PccChecker();
        this.addNum = 0;
        this.taskNum = taskNum;
        this.checkExecutorService = checkExecutorService;
    }

    public ConPccChecker(Checker checker, int taskNum, ExecutorService checkExecutorService) {
        super(checker);
        this.pcc = new PccChecker();
        this.addNum = 0;
        this.taskNum = taskNum;
        this.checkExecutorService = checkExecutorService;
    }

    @Override
    public boolean add(String patternId, Context context) {
        if (patternId.equals(stRoot.getContextSetName())) {
            addNum++;
        }
        return super.add(patternId, context);
    }

    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        int size = cctRoot.getChildTreeNodes().size();
        if (addNum == 0 || stMap.size() < 2 || size <= 1) { // 无新增分支，直接增量检测
            addNum = 0;
            return pcc.evaluation(cctRoot, param);
        }

        int workload = size;

        int workerNum = workload;

        if ( workerNum >= taskNum) {
            workerNum = taskNum;

        }

        List<Future<Result>> subResultList = new ArrayList<>();

        for(int i = 0; i < workerNum; i++) {
            List<Context> p = new ArrayList<>();
            p.addAll(param);
            Future<Result> subResult = checkExecutorService.submit(new CheckTask(cctRoot, p, new PccChecker(),i * workload / workerNum, (i + 1) * workload / workerNum - 1));
            subResultList.add(subResult);
        }

        boolean andValue = true;
        boolean orValue = false;
        StringBuilder satisfiedLink = new StringBuilder();
        StringBuilder violatedLink = new StringBuilder();
        try {
            for (Future<Result> subResult : subResultList) {
                Result tmpResult = subResult.get();
                boolean tmp = tmpResult.getValue();
                andValue = andValue && tmp;
                orValue = orValue || tmp;
                if (tmp) {
                    satisfiedLink.append(tmpResult.getLink());
                    satisfiedLink.append("#");
                } else {
                    violatedLink.append(tmpResult.getLink());
                    violatedLink.append("#");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        boolean value;
        if (cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
            value = andValue;
        } else {
            value = orValue;

        }
        cctRoot.setNodeValue(value);
        if(value) {
            int len = satisfiedLink.length();
            if(len > 0) {
                satisfiedLink.deleteCharAt(len - 1);
            }
            cctRoot.setLink(satisfiedLink.toString());
        } else {
            int len = violatedLink.length();
            if(len > 0) {
                violatedLink.deleteCharAt(len - 1);
            }
            cctRoot.setLink(violatedLink.toString());
        }

        cctRoot.setNodeStatus(NodeStatus.NC_STATE);

        addNum = 0; //新增分支数清零
        return cctRoot.getNodeValue();
    }

    @Override
    public boolean doCheck() {
        return super.doCheck();
    }

    @Override
    public void sCheck(List<Context> contextList) {
        CCTNode newRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        build(stRoot, newRoot, 3);

        List<Context> param = new ArrayList<>();
        evaluation(newRoot, param);
    }
}
