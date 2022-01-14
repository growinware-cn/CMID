package cn.edu.nju.util;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.Scanner;

public class Interaction {
    public static void say(String content, boolean isParted) {
        if (!isParted) {
            return ;
        }
        Scanner in = new Scanner(System.in);
        String str;
        while(true) {
            System.out.println("[INFO] 是否" + content + "（Y/N）：");
            str = in.nextLine();

            if ("y".equals(str.toLowerCase())) {
                break;
            }
            else if ("n".equals(str.toLowerCase())) {
                System.exit(0);
            }

        }
        return ;
    }

    public static boolean init(String argsString) {
        boolean isParted;
        if(argsString.equalsIgnoreCase("N"))
            isParted = false;
        else{
            Scanner in = new Scanner(System.in);

            String str;
            while(true) {
                System.out.println("[INFO] 是否分段启动（Y/N）：");
                str = in.nextLine();

                if ("y".equals(str.toLowerCase())) {
                    isParted = true;
                    break;
                } else if ("n".equals(str.toLowerCase())) {
                    isParted = false;
                    break;
                }
            }

        }

        return isParted;
    }

    public static String fileSay(String fileName, String filePath) {
        File file = new File(filePath);
        String retPath = filePath;
        if (file.exists()) {
            Scanner in = new Scanner(System.in);
            String str;
            while (true) {
                System.out.println("[INFO] " + fileName + filePath  + "已存在，是否覆盖（Y/N）：");
                str = in.nextLine();
                if("y".equals(str.toLowerCase())) {
                    file.delete();
                    break;
                }
                else if ("n".equals(str.toLowerCase())) {
                    do {
                        System.out.println("[INFO] 请输入新的" + fileName + "路径：");
                        retPath = in.nextLine();
                    } while (retPath.equals("") || new File(retPath).exists());
                    break;
                }
            }
        }

        file.getParentFile().mkdir();
        try {
            //创建文件
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retPath;
    }
}
