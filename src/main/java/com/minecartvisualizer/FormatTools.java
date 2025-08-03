package com.minecartvisualizer;

import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatTools {
    public static double truncate(double value,int accuracy) {
        BigDecimal bd = new BigDecimal(String.valueOf(value));
        bd = bd.setScale(accuracy, RoundingMode.DOWN);
        return bd.doubleValue();
    }

    public static Vec3d truncate(Vec3d vec, int accuracy) {
        double x = truncate(vec.x, accuracy);
        double y = truncate(vec.y, accuracy);
        double z = truncate(vec.z, accuracy);
        return new Vec3d(x, y, z);
    }
}
