package cn.edu.nju.util;

import java.text.SimpleDateFormat;


/**
 * Created by njucjc on 2017/10/23.
 */
public class TimestampHelper {
    private static SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    /**
     *返回两个时间戳对应时间差的毫秒数
     * @param timestamp1
     * @param timestamp2
     * @return 两个时间戳的时间差
     */
    public static long timestampDiff(String timestamp1, String timestamp2) {
        long diff = getDiff(timestamp1, timestamp2);
        return diff > 0 ? diff : -diff;
    }

    private static long getDiff(String timestamp1, String timestamp2) {
        long diff = 0;
        try {
            java.util.Date begin = dfs.parse(timestamp1);
            java.util.Date end = dfs.parse(timestamp2);
            diff = end.getTime() - begin.getTime();
        }catch (Exception e) {
            System.out.println("[INFO] 时间戳格式错误");
            System.exit(1);
        }
        return diff;
    }

    public static int timestampCmp(String timestamp1, String timestamp2) {
        long diff = getDiff(timestamp1, timestamp2);
        if(diff < 0) {
            return 1;
        } else if(diff > 0) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 计算timstamp加上一个毫秒数的timestamp
     * @param timestamp
     * @param millis
     * @return
     */
    public static String plusMillis(String timestamp, long millis) {
        String timestamp2 = null;
        try {
            java.util.Date begin = dfs.parse(timestamp);
            timestamp2 = dfs.format(new java.util.Date(begin.getTime() + millis));
        }catch (Exception e) {
            System.out.println("[INFO] 时间戳格式错误");
            System.exit(1);
        }
        return timestamp2;
    }

    public static String getCurrentTimestamp() {
        return dfs.format(new java.util.Date());
    }

    public static java.util.Date parserDate(String time) {
        java.util.Date date = null;
        try {
            date = dfs.parse(time);
        } catch (Exception e) {
            System.out.println("[INFO] 时间戳格式错误");
            System.exit(1);
        }
        return date;
    }


    public static void main(String[] args) {
        System.out.println(timestampDiff("2007-10-26 11:00:00:000","2007-10-26 11:00:00:057"));
        System.out.println(timestampDiff("2007-10-26 11:00:00:000","2007-10-26 11:00:00:228"));
        System.out.println(timestampDiff("2007-10-26 11:00:00:057","2007-10-26 11:00:00:228"));
        System.out.println(plusMillis("2007-10-26 11:00:00:057", 100));
    }
}
