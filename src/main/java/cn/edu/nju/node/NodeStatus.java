package cn.edu.nju.node;

/**
 * Created by njucjc on 2017/10/7.
 */
public interface NodeStatus {
    public static final int NC_STATE = 0; //No checking required
    public static final int PC_STATE = 1; //Partial checking
    public static final int EC_STATE = 2; //Entire checking

    int getNodeStatus();
    void setNodeStatus(int nodeStatus);
}
