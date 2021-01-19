package cn.edu.nju.scheduler;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

import java.util.*;


/**
 * Created by njucjc at 2018/3/19
 */
public class GEAScheduler implements Scheduler{
    private List<Checker> checkerList;

    private Map<String, Boolean> scheduleMap;

    private Map<String, List<String>> currentBatchMap;

    protected ContextParser parser = new ContextParser();

    private List<Context> contextList;

    private Map<String, int []> winSizeMap;


    public GEAScheduler(List<Checker> checkerList) {
        this.checkerList = checkerList;
        this.scheduleMap = new HashMap<>();
        this.currentBatchMap = new HashMap<>();
        this.contextList = new ArrayList<>();
        this.winSizeMap = new HashMap<>();


        for(Checker checker : checkerList) {
            String name = checker.getName();
            scheduleMap.put(name, false);
            currentBatchMap.put(name, new ArrayList<>());
            int [] tmp = new int[]{0, 0};
            winSizeMap.put(name, tmp);
        }
    }

    @Override
    public void update(String change) {

        for(Checker checker : checkerList) {
            if (matching(checker, change)) {
                scheduleMap.put(checker.getName(), true);
            }
        }
    }

    @Override
    public boolean schedule(String ruleName) {
        boolean result = scheduleMap.get(ruleName);
        if(result) {
            scheduleMap.put(ruleName, false);
        }
        return result;
    }

    private boolean matching(Checker checker, String change) {
        String [] elements = change.split(",");

        if (!checker.affected(elements[1])) {
            return false;
        }

        List<String> currentBatch = currentBatchMap.get(checker.getName());

        boolean result = sCondition(checker, currentBatch, elements);

        Context context = parser.parseChangeContext(elements);
        contextList.add(context);

        List<Boolean> subTree = calcSubTree(checker, elements[1], context);


        if (result) { //make batch empty
            updateWinSize(checker.getName(), currentBatch.size());
            currentBatch.clear();
        } else {
            String c = cCondition(checker, currentBatch, elements, subTree);
            if (c == null) {
                currentBatch.add(change);
            }
            else { //GEAS-opt only
                updateGEASOptWinSize(checker.getName());
                currentBatch.remove(c);
            }
        }

        return result;
    }

    private boolean sCondition(Checker checker, List<String> currentBatch, String [] elements) {
        boolean result = false;

        for(String c : currentBatch) {
            String [] e = c.split(",");
            if(checker.isInIncAddSet(e[0] + "," + e[1]) && checker.isInIncDelSet(elements[0] + "," + elements[1])) {
                result = true;
                break;
            }
        }

        return result;
    }

    protected String cCondition(Checker checker, List<String> currentBatch, String [] elements, List<Boolean> subTree) {
        return null;
    }

    protected List<Boolean> calcSubTree(Checker checker, String patternId, Context c){
        List<Boolean> tmp = checker.calcSubTree(patternId, c);
        for(String c1 : currentBatchMap.get(checker.getName())) {
            List<Boolean> l1 = checker.calcSubTree(patternId, parser.parseChangeContext(c1.split(",")));

            for (int i = 0; i < tmp.size(); i++) {
                if (tmp.get(i) == l1.get(i)) {
                    continue;
                }
            }

        }
        return tmp;
    }

    @Override
    public void reset() {
        for(String key : scheduleMap.keySet()) {
            scheduleMap.put(key, true);
        }
    }

    void updateWinSize(String name, int size) {
        int [] tmp = winSizeMap.get(name);
        tmp[0] += size;
        tmp[1]++;
    }

    void updateGEASOptWinSize(String name) {
        int [] tmp = winSizeMap.get(name);
        tmp[0] += 2;
    }

    @Override
    public int getWinSize() {
        int winSum = 0;
        int count = 0;
        for (Checker checker : checkerList) {
            int [] tmp = winSizeMap.get(checker.getName());
            winSum += tmp[0];
            count += tmp[1];
        }

        return count == 0 ? 0 : winSum/count;

    }
}
