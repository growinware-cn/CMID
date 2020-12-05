package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            Thread checkerThread = new Thread(new CheckerBuilder(args[0]));
            checkerThread.setPriority(Thread.MAX_PRIORITY);
            checkerThread.start();
//            CheckerBuilder builder = new CheckerBuilder(args[0]);
//            builder.run();
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }
}
