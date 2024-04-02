package com.example.app_3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SenseUtility {

    private static SenseUtility senseUtility;
    public final float REAL_WORLD_HEIGHT_C1 = 2.54f; // meters

    public final int CONVERGENCE_LIMIT = 18;

    public final int TOTAL_NUM_PARTICLES = 1300;
    public final float REAL_WORLD_WIDTH_C1 = 4.8f; // meters
    public final float PIXEL_SCALE = 57.0f; // 57 pixels = 1 meter
    public final float WALL_THICKNESS = 15.0f;
    public final double NOISE_MEAN = 0.0;
    public final double PARTICLE_NOISE_MEAN = 0.0;
    public final double NOISE_STANDARD_DEVIATION = 12.5   ;
    public final double PARTICLE_NOISE_STANDARD_DEVIATION = 1;

    private final double ORIENTATION_NOISE_RANGE = Math.PI / 12;
    public final Random random = new Random();
    public Float OFFSET_IN_RADIANS = Float.valueOf((float) Math.toRadians(30));
    public final float STEP_LENGTH = (float) (0.48 * PIXEL_SCALE);
    private ArrayList<Double> accelerometerRMSList = new ArrayList<>();
    private ArrayList<Double> gyroscopeRMSList = new ArrayList<>();
    private ArrayList<Double> acceleroGyroRMSList = new ArrayList<>();
    private List<CellConfiguration> cellLayout;
    private List<ParallelogramConfiguration> parallelogramLayout;
    private Double accelerometerX = Double.valueOf(0);
    private Double accelerometerY = Double.valueOf(0);
    private Double accelerometerZ = Double.valueOf(0);
    private Float gyroscopeX = Float.valueOf(0);
    private Float gyroscopeY = Float.valueOf(0);
    private Float gyroscopeZ = Float.valueOf(0);
    private Float currentAzimuthInDegrees = Float.valueOf(0);
    private Float currentAzimuthInRadians = Float.valueOf(0);

    private SenseUtility() {

    }

    public static SenseUtility getSenseUtilityInstance() {
        if (senseUtility == null) {
            senseUtility = new SenseUtility();
        }
        return senseUtility;

    }

    public List<CellConfiguration> getWallLayout() {
        return cellLayout;
    }

    public void setWallLayout(List<CellConfiguration> wallLayout) {
        this.cellLayout = wallLayout;
    }

    public List<ParallelogramConfiguration> getParallelogramLayout() {
        return parallelogramLayout;
    }

    public void setParallelogramLayout(List<ParallelogramConfiguration> parallelogramLayout) {
        this.parallelogramLayout = parallelogramLayout;
    }

    public Float getCurrentAzimuthInRadiansWithOffset() {
        return currentAzimuthInRadians + OFFSET_IN_RADIANS;
    }

    public Float getCurrentAzimuthInRadians() {
        return currentAzimuthInRadians;
    }

    public void setCurrentAzimuthInRadians(Float currentAzimuthInRadians) {
        this.currentAzimuthInRadians = currentAzimuthInRadians;
    }

    public Float getCurrentAzimuthInDegrees() {
        return currentAzimuthInDegrees;
    }

    public void setCurrentAzimuthInDegrees(Float currentAzimuthInDegrees) {
        this.currentAzimuthInDegrees = currentAzimuthInDegrees;
    }

    public ArrayList<Double> getAccelerometerRMSList() {
        return accelerometerRMSList;
    }

    public void setAccelerometerRMSList(ArrayList<Double> accelerometerRMSList) {
        this.accelerometerRMSList = accelerometerRMSList;
    }

    public ArrayList<Double> getGyroscopeRMSList() {
        return gyroscopeRMSList;
    }

    public void setGyroscopeRMSList(ArrayList<Double> gyroscopeRMSList) {
        this.gyroscopeRMSList = gyroscopeRMSList;
    }

    public ArrayList<Double> getAcceleroGyroRMSList() {
        return acceleroGyroRMSList;
    }

    public void setAcceleroGyroRMSList(ArrayList<Double> acceleroGyroRMSList) {
        this.acceleroGyroRMSList = acceleroGyroRMSList;
    }

    public Double getAccelerometerX() {
        return accelerometerX;
    }

    public void setAccelerometerX(Double accelerometerX) {
        this.accelerometerX = accelerometerX;
    }

    public Double getAccelerometerY() {
        return accelerometerY;
    }

    public void setAccelerometerY(Double accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public Double getAccelerometerZ() {
        return accelerometerZ;
    }

    public void setAccelerometerZ(Double accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public Float getGyroscopeX() {
        return gyroscopeX;
    }

    public void setGyroscopeX(Float gyroscopeX) {
        this.gyroscopeX = gyroscopeX;
    }

    public Float getGyroscopeY() {
        return gyroscopeY;
    }

    public void setGyroscopeY(Float gyroscopeY) {
        this.gyroscopeY = gyroscopeY;
    }

    public Float getGyroscopeZ() {
        return gyroscopeZ;
    }

    public void setGyroscopeZ(Float gyroscopeZ) {
        this.gyroscopeZ = gyroscopeZ;
    }


    public double customGaussianNoise(double mean, double standardDeviation) {
        int n = 12;  // Number of uniform random numbers to sum
        double sum = 0;

        for (int i = 0; i < n; i++) {
            sum += Math.random();
        }

        // Scale and shift sum to achieve desired mean and standard deviation
        return mean + standardDeviation * (sum - n / 2.0) / Math.sqrt(n / 12.0);
    }


    public double getRandomOrientationNoiseForParticles() {
        double minValue = -ORIENTATION_NOISE_RANGE;
        double maxValue = ORIENTATION_NOISE_RANGE;
        return minValue + (maxValue - minValue) * random.nextDouble();
    }

}
