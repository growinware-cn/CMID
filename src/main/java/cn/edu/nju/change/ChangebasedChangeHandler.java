package cn.edu.nju.change;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.context.Context;
import java.util.List;
import java.util.Map;

/**
 * Created by njucjc at 2018/1/23
 */
public class ChangebasedChangeHandler extends ChangeHandler {
    public ChangebasedChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        super(patternMap, checkerMap, scheduler, checkerList);
    }

    @Override
    public void doContextChange(int num, String change) {
        scheduler.update(change);
        doCheck();

        String [] strs = change.split(",");

        String op = strs[0];
        String patternId = strs[1];

        if (op.equals("+")) {
            additionChange(patternId, parseContext(num, change));
        }
        else if (op.equals("-")) {
            deleteChange(parseContext(num, change).getTimestamp(), patternId);
        }
        else {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.changeFilePath + "'文件中存在不可识别操作类型：" + op);
            System.exit(1);
        }

    }
}
