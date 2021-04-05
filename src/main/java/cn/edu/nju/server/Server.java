package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{
    private DatagramSocket serverSocket;
    private boolean running;
    private int port = 8000;
    private byte [] buf = new byte[256];

    public Server(String configFilePath)  {
        super(configFilePath);
        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;


        long count = 0;

        long timeSum = 0;

        System.out.println("[INFO] Sever启动完毕，等待Client连接并启动一致性检测......");
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String msg = new String(packet.getData(),0, packet.getLength());
                if ("exit".equals(msg)) {
                    System.out.println();
                    System.out.println("[INFO] 一致性检测结束，Server关闭......");
                    running = false;
                    break;
                }


                int num = Integer.parseInt(msg.substring(0, msg.indexOf(",")));
                long interval = Long.parseLong(msg.substring(msg.lastIndexOf(",")+1));
                assert count != -1:"counter overflow.";
//                if(onDemand && switcher.isSwitch(num, interval)) {
//
//                    switchPoint.add(TimestampHelper.timestampDiff(TimestampHelper.getCurrentTimestamp(), startTimestamp));
//                    long startUpdate = System.nanoTime();
//                    update(switcher.getCheckerType(), switcher.getSchedulerType());
//                    long endUpdate = System.nanoTime();
//                    switchTimeCount += endUpdate - startUpdate;
//                }

                msg = msg.substring(msg.indexOf(",") + 1, msg.lastIndexOf(","));

                long start = System.nanoTime();

                changeHandler.doContextChange(num, msg);
                count++;

                long end = System.nanoTime();
                long checkTime = (end - start);
                timeSum += checkTime;

                int inc = 0;
                for (Checker checker : checkerList) {
                    inc += checker.getInc();
                }

                System.out.print( "[INFO] Send/Receive: " + (num + 1) + "/" + count +
                        "\tTotal inc: "+ inc +
                        "\tTotal Checking time: " + (timeSum/1000000)  +" ms\r");


            }
            scheduler.reset();
            changeHandler.doCheck();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        changeHandler.shutdown();
        int inc = 0;
        long time = 0L;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
            time = time + checker.getTimeCount();
        }
        LogFileHelper.getLogger().info("Total Inc: " + inc, true);
        LogFileHelper.getLogger().info("Total checking time: " +  timeSum / 1000000 + " ms", true);
        accuracy(LogFileHelper.logFilePath);
        shutdown();
    }

    public static void main(String[] args) {
        if(args.length == 1) {
            Thread serverThread = new Thread(new Server(args[0]));
            serverThread.setPriority(Thread.MAX_PRIORITY);
            serverThread.start();
        }
        else {
            System.out.println("Usage: java Server [configFilePath].");
        }
    }
}
