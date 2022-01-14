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
        logFilePath = Interaction.fileSay("日志文件", log);
        System.out.println("[INFO] 日志文件为" + logFilePath);
        LOGGER = new Logger(logFilePath, true);
    }

    public synchronized static Logger getLogger() {
        return LOGGER;
    }

    public static void main(String  args[]) {
        getLogger().info("Hello World.", false);
        LOGGER.info("123", false);
    }

}

