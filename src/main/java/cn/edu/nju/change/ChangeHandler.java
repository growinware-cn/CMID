package cn.edu.nju.change;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;

import java.util.List;
import java.util.Map;

/**
 * Created by njucjc at 2018/1/23
 */
public abstract class ChangeHandler {
    protected Map<String, Pattern> patternMap;

    protected Map<String, Checker> checkerMap;

    protected Scheduler scheduler;

    protected List<Checker> checkerList;

    protected ContextParser contextParser;

    public long timeCount = 0L;

    public ChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        this.patternMap = patternMap;
        this.checkerMap = checkerMap;
        this.scheduler = scheduler;
        this.checkerList = checkerList;
        this.contextParser = new ContextParser();

    }

    private String getClassString(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }

    protected Context parseContext(int num, String change) {
        String [] strs = change.split(",");
        if(strs[0].equals("+") || strs[0].equals("-")) {
            return contextParser.parseChangeContext(strs);
        }
        else {
            return contextParser.parseContext(num, change);
        }
    }
    public void doCheck() {
        long start = System.nanoTime();
        for(Checker checker : checkerList) {
            if(scheduler.schedule(checker.getName())) {
                checker.doCheck();
            }
        }
        long end = System.nanoTime();
        timeCount += (end -start);
    }

    protected final void deleteChange(String timestamp, String  patternId) {
        Pattern pattern = patternMap.get(patternId);
        if (pattern == null) {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.changeFilePath + "'文件中存在不可识别操作内容：" + patternId);
            System.exit(1);
        }
        Checker checker = checkerMap.get(patternId);
        checker.delete(patternId, timestamp);
     //   pattern.deleteFirstByTime(timestamp);
    }

    protected final void additionChange(String patternId, Context context) {
        Pattern pattern = patternMap.get(patternId);
        if (pattern == null) {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.changeFilePath + "'文件中存在不可识别操作内容：" + patternId);
            System.exit(1);
        }
        if(pattern.isBelong(context)) {
    //        pattern.addContext(context);
            Checker checker = checkerMap.get(pattern.getId());
            checker.add(pattern.getId(),context);
        }
    }

    public abstract void doContextChange(int num, String change);

    public void shutdown() {}

    public void update(Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        this.checkerMap = checkerMap;
        this.scheduler = scheduler;
        this.checkerList = checkerList;
    }

}
