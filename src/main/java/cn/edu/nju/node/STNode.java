package cn.edu.nju.node;

/**
 * Created by njucjc on 2017/10/7.
 */
public class STNode extends TreeNode implements NodeType{
    private int nodeType;

    private String contextSetName;

    private int nodeNum = 0;

    public STNode(String nodeName, int nodeType) {
        super(nodeName);
        this.nodeType = nodeType;
        this.contextSetName = "";
    }

    public STNode(String nodeName, int nodeType, String contextSetName) {
        super(nodeName);
        synchronized (this) {
            this.nodeType = nodeType;
            this.contextSetName = contextSetName;
        }
    }

    public  STNode() {
        super("");
        this.nodeType = EMPTY_NODE;
        this.contextSetName = "";
    }

    public String getContextSetName() {
        return contextSetName;
    }

    public void setContextSetName(String contextSetName) {
        this.contextSetName = contextSetName;
    }

    @Override
    public int getNodeType() {
        return nodeType;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    @Override
    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }
}
