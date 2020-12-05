package cn.edu.nju.node;

import cn.edu.nju.context.Context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class CCTNode extends TreeNode implements NodeType, NodeStatus{

    /* Subtree value */
    private boolean nodeValue;

    /* Context assignment */
    private Context context;

    /* Tree node state */
    private int nodeStatus;

    private int nodeType;

    private String link;


    public CCTNode(String nodeName, int nodeType, Context context) {
            super(nodeName);
        this.nodeValue = false;
        this.context = context;
        this.nodeStatus = CCTNode.EC_STATE;
        this.nodeType = nodeType;
        this.link = "";
    }

    public CCTNode(String nodeName, int nodeType) {
        super(nodeName);
        this.nodeValue = false;
        this.context = null;
        this.nodeStatus = CCTNode.EC_STATE;
        this.nodeType = nodeType;
        this.link = "";
    }

    public boolean getNodeValue() {
        return nodeValue;
    }

    public Context getContext() {
        return context;
    }

    public void setNodeValue(boolean nodeValue) {
        this.nodeValue = nodeValue;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int getNodeStatus() {
        return nodeStatus;
    }

    @Override
    public void setNodeStatus(int nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    @Override
    public int getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }
}
