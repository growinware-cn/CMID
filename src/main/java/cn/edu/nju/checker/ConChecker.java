package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by njucjc on 2017/12/27.
 */

public class ConChecker extends EccChecker {
    private int taskNum;

    private Checker ecc;

    private ExecutorService checkExecutorService;

    public ConChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum, ExecutorService checkExecutorService) {
        super(name, stRoot, patternMap, stMap);
        this.taskNum = taskNum;
        this.ecc = new EccChecker();
        this.checkExecutorService = checkExecutorService;//Executors.newFixedThreadPool(taskNum);
    }

    public ConChecker(Checker checker, int taskNum, ExecutorService checkExecutorService) {
        super(checker);
        this.taskNum = taskNum;
        this.ecc = new EccChecker();
        this.checkExecutorService = checkExecutorService;
    }

    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {

        int workload = cctRoot.getChildTreeNodes().size();
        int workerNum = workload;
        if ( workerNum >= taskNum) {
            workerNum = taskNum;
        }

        List<Future<Result>> subResultList = new ArrayList<>();

        for(int i = 0; i < workerNum; i++) {
            List<Context> p = new ArrayList<>();
            p.addAll(param);
            Future<Result> subResult = checkExecutorService.submit(new CheckTask(cctRoot, p, ecc,i * workload / workerNum, (i + 1) * workload / workerNum - 1));
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
        return value;
    }

    @Override
    protected void removeCriticalNode(STNode stRoot, CCTNode cctRoot) {
        clearCCTMap();
    }

}


