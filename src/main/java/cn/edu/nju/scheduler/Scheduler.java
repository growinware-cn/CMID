package cn.edu.nju.scheduler;

/**
 * Created by njucjc on 2017/10/23.
 */
public interface Scheduler {
    void reset();
    void  update(String change);
    boolean schedule(String ruleName);
    //----------------------
    int getWinSize();
}
