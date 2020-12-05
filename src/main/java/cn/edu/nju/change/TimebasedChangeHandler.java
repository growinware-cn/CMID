package cn.edu.nju.change;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by njucjc at 2018/1/23
 */
public class TimebasedChangeHandler extends ChangeHandler{
    public TimebasedChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        super(patternMap, checkerMap, scheduler, checkerList);
    }

    @Override
    public void doContextChange(int num, String change) {
        Context context = parseContext(num, change);
        Set<String> timeSet = new TreeSet<>();
        //按升序收集过期时间
        for(String key : patternMap.keySet()) {
            Pattern pattern = patternMap.get(key);
            timeSet.addAll(pattern.getOutOfDateTimes(context.getTimestamp()));
        }

        //按时间顺序删除context
        for(String timestamp : timeSet) {
            for (String patternId : patternMap.keySet()) {
                deleteChange(timestamp, patternId);
            }
            scheduler.update("");//time-based不需要参数
            doCheck();
        }

        //在相关的pattern里添加context
        // System.out.println("[DEBUG] '+' " + context.toString());
        for(String patternId : patternMap.keySet()) {
            additionChange(patternId, context);
        }
        scheduler.update("");//time-based不需要参数
        doCheck();
    }
}
