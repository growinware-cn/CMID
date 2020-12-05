package cn.edu.nju.util;

import java.util.*;

public class HotAreaHelper {

    private static final Map<String, Polygon> hotAreaMap = new HashMap<>();

    static {
        hotAreaMap.put("A", new Polygon(new double[]{22.571615, 22.573121, 22.590556, 22.590873},
                                        new double[]{113.923059, 113.864853, 113.882534, 113.90176}));
        hotAreaMap.put("B", new Polygon(new double[]{22.548391, 22.573121, 22.590556, 22.590873},
                                        new double[]{113.89455, 113.864853, 113.882534, 113.90176}));
        hotAreaMap.put("C", new Polygon(new double[]{22.571615, 22.548391, 22.573121, 22.590556, 22.590873},
                                        new double[]{113.923059, 113.89455, 113.864853, 113.882534, 113.901761}));
        hotAreaMap.put("D", new Polygon(new double[]{22.559489, 22.570902, 22.503359},
                                        new double[]{114.02018, 114.085411, 114.060348}));
        hotAreaMap.put("E", new Polygon(new double[]{22.559489, 22.571853, 22.541416, 22.532457},
                                        new double[]{114.092304, 114.142402, 114.135879, 114.08485}));
        hotAreaMap.put("F", new Polygon(new double[]{22.559489, 22.570902, 22.571853, 22.541416, 22.503359},
                                        new double[]{114.02018, 114.085411, 114.142402, 114.135879, 114.060348}));
        hotAreaMap.put("G", new Polygon(new double[]{22.565195, 22.55616, 22.528414, 22.514936, 22.531744},
                                        new double[]{113.927826, 114.015056, 114.019691, 113.937809, 113.902618}));
        hotAreaMap.put("H", new Polygon(new double[]{22.565195, 22.55616, 22.611317},
                                        new double[]{113.927826, 114.015056, 113.988792}));
        hotAreaMap.put("I", new Polygon(new double[]{22.531744, 22.565195, 22.55616, 22.528414, 22.514936},
                                        new double[]{113.902618, 113.927826, 114.015056, 114.019691, 113.937809}));
    }

    public static boolean inArea(String name, double x, double y) {
        Polygon poly = hotAreaMap.get(name);
        double[] vertX= poly.getX();
        double[] vertY = poly.getY();
        int nVert = poly.getnVert();

        int i, j;
        boolean c = false;
        for (i = 0, j = nVert-1; i < nVert; j = i++) {
            if (((vertY[i]>y) != (vertY[j]>y)) &&
                    (x<(vertX[j]-vertX[i]) * (y-vertY[i]) / (vertY[j]-vertY[i]) + vertX[i]))
                c = !c;
        }
        return c;
    }

}

class Polygon {
    private int nVert;
    private double [] x;
    private double [] y;

    public Polygon(double[] x, double[] y) {
        this.x = x;
        this.y = y;
        this.nVert = this.x.length;
    }

    public double[] getX() {
        return x;
    }

    public double[] getY() {
        return y;
    }

    public int getnVert() {
        return nVert;
    }
}
