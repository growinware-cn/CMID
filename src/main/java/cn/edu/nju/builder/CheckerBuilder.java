package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.LogFileHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder(String configFilePath) {
        super(configFilePath);
    }

    @Override
    public void run() {
        List<String> contextList;
        if("time".equals(this.changeHandlerType.split("-")[1])) {
            contextList = fileReader(this.dataFilePath);
        }
        else {
            contextList = fileReader(this.changeFilePath);
        }
        System.out.println("[INFO] 开始一致性检测......");
        int count = 0;
        long startTime = System.nanoTime();
        for(String change : contextList) {
            System.out.print("[INFO] 当前进度: " + (count + 1) + "/" + contextList.size() + '\r');
            changeHandler.doContextChange(count, change);
            count++;
        }
        scheduler.reset();
        changeHandler.doCheck();
        long endTime = System.nanoTime(); //获取结束时间
        int incCount = 0;
        for(Checker checker : checkerList) {
            incCount += checker.getInc();
        }
        System.out.println();
        System.out.println("[INFO] 一致性检测完毕......");
        LogFileHelper.getLogger().info("Total INC: " + incCount, true);
        LogFileHelper.getLogger().info("Total checking time: " + (endTime - startTime) / 1000000 + " ms", true);
        accuracy(LogFileHelper.logFilePath);
        shutdown();
    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("config.properties");
    }


}
