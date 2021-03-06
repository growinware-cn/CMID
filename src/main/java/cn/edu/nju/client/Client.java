package cn.edu.nju.client;

import cn.edu.nju.util.ChangeFileHelper;
import cn.edu.nju.util.Interaction;
import cn.edu.nju.util.TimestampHelper;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Client implements Runnable{
    private DatagramSocket socket;
    private InetAddress address;
    int port = 8000;
    private static boolean isParted = false;

    private List<String> contextStrList;
    private List<Long> sleepTime;


    public Client(String contextFilePath)  {
        this.contextStrList = new ArrayList<>();
        this.sleepTime = new ArrayList<>();

        isParted = Interaction.init("");
        System.out.println("[INFO] 客户端开始启动");
        Interaction.say("进行端口绑定", isParted);
        System.out.println("[INFO] 成功绑定" + port + "端口，链接建立");
        Interaction.say("进行读取时间戳", isParted);

        try {
            FileReader fr = new FileReader(contextFilePath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            String lastLine = br.readLine();
            int timestampIndex = 0;
            if(lastLine.contains("+")) { //第一行总是增加
                timestampIndex = 3;
            }
            contextStrList.add(lastLine);
            while ((line = br.readLine()) != null) {
                contextStrList.add(line);
                long diff = TimestampHelper.timestampDiff(line.split(",")[timestampIndex], lastLine.split(",")[timestampIndex]);
                sleepTime.add(diff);
                lastLine = line;
            }
            sleepTime.add(1L);

            System.out.println("[INFO] 时间戳读取成功");
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        Interaction.say("开始发送数据", isParted);
        long sleepMillis = 0;
        long totalTime = 0;
        long startTime = System.nanoTime();
        long endTime = 0;
        for (int i = 0; i < contextStrList.size(); i++){

            System.out.print("Send " + (i + 1) + "/" + contextStrList.size() + "\r");
            sleepMillis = sleepTime.get(i);
            sendMsg(i+ "," + contextStrList.get(i) + "," + sleepMillis);
            endTime = System.nanoTime();

            long diff = (endTime - startTime) - totalTime * 1000000;
            totalTime += sleepMillis;

//            assert diff >= 0 : "Time error !";

            sleepMillis = (sleepMillis - diff / 1000000) > 0 ? (sleepMillis - diff / 1000000) : 0;

            try {
                Thread.sleep(sleepMillis);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        endTime = System.nanoTime();
        System.out.println();
        System.out.println("[INFO] 数据文件发送结束");

        Interaction.say("开始广播结束报文关闭链接，关闭客户端", isParted);
        for (int i = 0; i < 10000; i++) {
            sendMsg("exit");
        }
        System.out.println("[INFO] 成功广播结束报文关闭链接，客户端关闭");

    }

    private void sendMsg(String msg) {
        byte [] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            Properties properties = new Properties();
            try {
                if (!new File(args[0]).exists()) {
                    System.out.println("[INFO] 配置文件不存在: " + args[0]);
                    System.exit(1);
                }

                FileInputStream fis = new FileInputStream(args[0]);
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String dataFilePath = properties.getProperty("dataFilePath");
            String patternFilePath = properties.getProperty("patternFilePath");
            String changeHandlerType = properties.getProperty("changeHandlerType");

            Thread client;
            if("time".equals(changeHandlerType.split("-")[1])) {
                ChangeFileHelper changeFileHelper = new ChangeFileHelper(patternFilePath);
                dataFilePath = changeFileHelper.parseChangeFile(dataFilePath);
            }
            client = new Thread(new Client(dataFilePath));
            client.setPriority(Thread.MAX_PRIORITY);
            client.start();
        }
        else {
            System.out.println("Usage: java Client [configFilePath].");
        }
    }
}
