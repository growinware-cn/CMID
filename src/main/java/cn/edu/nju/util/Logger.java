package cn.edu.nju.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Logger {
    private BufferedWriter out;

    public Logger(String file) {
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void info(String msg, boolean terminal) {
        if (terminal) {
            System.out.println(msg);
        }

        try {
            out.write(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
