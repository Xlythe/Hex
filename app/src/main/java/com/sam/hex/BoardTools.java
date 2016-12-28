package com.sam.hex;

public class BoardTools {
    public static double radiusCalculator(double w, double h, double n) {
        double spaceV = (((n - 1) * 3 / 2) + 2);
        double spaceH = n + (n - 1) / 2; // always bigger.
        spaceH = (w / (spaceH * Math.sqrt(3)));
        spaceV = (h / spaceV);
        if (spaceV < spaceH) {
            return spaceV;
        }
        return spaceH;
    }
}
