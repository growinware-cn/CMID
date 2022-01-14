package cn.edu.nju.context;

import cn.edu.nju.builder.AbstractCheckerBuilder;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        if (fields.length != 7) {
            System.out.println("[INFO] "+ AbstractCheckerBuilder.dataFilePath + "文件格式错误");
            System.exit(1);
        }

        String timestamp = null;
        String plateNumber = null;
        double longitude = 0.0;
        double latitude = 0.0;
        double speed = 0.0;
        int status = 0;

        try {
            timestamp = fields[0];
            plateNumber = fields[1];
            longitude = Double.parseDouble(fields[2]);
            latitude = Double.parseDouble(fields[3]);
            speed = Double.parseDouble(fields[4]);
            status = Integer.parseInt(fields[6]);

        } catch (NumberFormatException e) {
            System.out.println("[INFO] "+ AbstractCheckerBuilder.dataFilePath + "文件格式错误");
            System.exit(1);
        }

        return new Context(id, timestamp, plateNumber, longitude, latitude, speed, status);
    }

    public Context parseChangeContext(String [] elements) {


        Context context = null;
        context = new Context(Integer.parseInt(elements[2]),
                    elements[3],
                    elements[4],
                    Double.parseDouble(elements[5]),
                    Double.parseDouble(elements[6]),
                    Double.parseDouble(elements[7]),
                    Integer.parseInt(elements[8]));
        return context;
    }
}
