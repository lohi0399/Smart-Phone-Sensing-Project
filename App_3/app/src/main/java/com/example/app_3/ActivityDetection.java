package com.example.app_3;

import android.util.Log;

import java.util.ArrayList;

public class ActivityDetection {
    public static final int windowSize = 30;
    private static final double threshold = 0.42;
    private static final double standingLowerThreshHold = 0.48;
    private static final double standingHigherThreshHold = 0.51;

    public String calculateAutoCorrelation(ArrayList<Double> arr) {
        for (int i = 0; i < windowSize; i++) {
            ArrayList<Double> windowArr = new ArrayList<>(arr.subList(i, i + windowSize));
            double std_dev = calculateStandardDeviation(windowArr);
            double norm = calculateNorm(windowArr, std_dev);
            double autocorr = calculateAutoCorr(windowArr, std_dev, norm);
            Log.d("CUSTOMLOG", String.valueOf(autocorr));

            if ((autocorr > standingLowerThreshHold) && (autocorr < standingHigherThreshHold)) {
                return "STANDING";
            } else {
                return "WALKING";
            }
        }
        return null;
    }

    private double calculateStandardDeviation(ArrayList<Double> arr) {
        double mean = 0.0;
        double num = 0.0;

        for (double val : arr) {
            mean += val;
        }

        mean /= arr.size();

        for (double val : arr) {
            num += Math.pow(val - mean, 2);
        }

        return Math.sqrt(num / arr.size());
    }

    private double calculateNorm(ArrayList<Double> arr, double std_dev) {
        double norm = 0.0;

        for (double val : arr) {
            norm += Math.pow(val - std_dev, 2);
        }

        return norm;
    }

    private double calculateAutoCorr(ArrayList<Double> arr, double std_dev, double norm) {
        double sum = 0.0;

        for (int i = 0; i < windowSize / 2; i++) {
            sum += (arr.get(i) - std_dev) * (arr.get(i + windowSize - 1 - i) - std_dev);
        }

        return sum / norm;
    }
}
