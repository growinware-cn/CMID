package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LinkHelper;
import cn.edu.nju.util.LogFileHelper;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by njucjc on 2017/10/7.
 */
public class PccChecker extends Checker{

    public PccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap) {
        super(name, stRoot, patternMap, stMap);
    }

    public  PccChecker(Checker checker) {
        super(checker);
    }

    protected PccChecker() {

    }
    /**
     *
     * @return violated link
     */
    @Override
    public boolean doCheck() {

        checkTimes++;
        List<Context> param = new CopyOnWriteArrayList<>();
        evaluation(cctRoot, param); //PCC计算

        boolean value = true;

        clearCriticalSet();

        if (!cctRoot.getNodeValue()) {
            String [] links = LinkHelper.splitLinks(cctRoot.getLink());
            for (String link : links) {

                addCriticalSet(link);

                if (addIncLink(link)) {
                    LogFileHelper.getLogger().info(getName() + " " + link,false);
                }
            }

            this.maxLinkSize = this.maxLinkSize < links.length ? links.length : this.maxLinkSize;
            value = false;
        }

        return value;
    }

    /**
     * 根据结点状态来判定是否需要重新计算value和link
     * @param cctRoot
     * @param param
     * @return
     */
    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        if(cctRoot.getNodeStatus() == CCTNode.NC_STATE) { //无需重算就直接返回
            return cctRoot.getNodeValue();
        }
        return super.evaluation(cctRoot, param);
    }

    @Override
    public void sCheck(List<Context> contextList) {
        CCTNode newRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        build(stRoot, newRoot, 2);

        List<Context> param = new ArrayList<>();
        evaluation(newRoot, param);
    }
}
