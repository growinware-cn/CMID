package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.Interaction;
import cn.edu.nju.util.LogFileHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder(String configFilePath, String argsString) {
        super(configFilePath, argsString);
    }

    @Override
    public void run() {
        Interaction.say("进入一致性检测处理", isParted);
        List<String> contextList;
        contextList = fileReader(this.dataFilePath);
        System.out.println("[INFO] 开始一致性处理");
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
        System.out.println("[INFO] 一致性检测过程完成，共检测出" + incCount + "个一致性错误");
        System.out.println("[INFO] 检测时间为" + (endTime - startTime) / 1000000 + " ms");
        LogFileHelper.getLogger().info("Total INC: " + incCount, false);
        LogFileHelper.getLogger().info("Total checking time: " + (endTime - startTime) / 1000000 + " ms", false);

        Interaction.say("进入结果分析", isParted);
        accuracy(LogFileHelper.logFilePath);
        shutdown();

    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("config.properties","");
    }


}
