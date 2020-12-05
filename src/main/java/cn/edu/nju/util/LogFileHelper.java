package cn.edu.nju.util;

import java.io.File;
import java.util.Scanner;

/**
 * Created by njucjc on 2017/10/13.
 */
public class LogFileHelper {
    private LogFileHelper() {}
    private static Logger LOGGER;
    public static String logFilePath;
    public static void  initLogger(String log) {
        logFilePath = log;
        File file = new File(logFilePath);
        if (file.exists()) {
            System.out.println("[INFO] 日志文件" + "'" + logFilePath + "'" + "已存在，是否覆盖（Y/N）：");
            Scanner in = new Scanner(System.in);
            String str;
            while (true) {
                str = in.nextLine();
                if("y".equals(str.toLowerCase())) {
                    file.delete();
                    break;
                }
                else if ("n".equals(str.toLowerCase())) {
                    do {
                        System.out.println("[INFO] 请输入新的日志文件路径：");
                        logFilePath = in.nextLine();
                    } while (logFilePath.equals("") || new File(logFilePath).exists());
                    break;
                }
                else {
                    System.out.println("[INFO] 是否覆盖，请输入（Y/N）：");
                }
            }
        }
        System.out.println("[INFO] 日志文件：" + logFilePath);
        LOGGER = new Logger(logFilePath);
    }

    public synchronized static Logger getLogger() {
        return LOGGER;
    }

    public static void main(String  args[]) {
        getLogger().info("Hello World.", false);
        LOGGER.info("123", false);
    }

}

