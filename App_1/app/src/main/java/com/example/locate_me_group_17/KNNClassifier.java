package com.example.locate_me_group_17;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KNNClassifier {
    private int k;
    private List<Sample> samples;

    public KNNClassifier(int k, List<Sample> samples) {
        this.k = k;
        this.samples = samples;
    }


//    public static List<KNNClassifier.Sample> readSamplesFromCsv(Context context, String fileName) {
//        List<KNNClassifier.Sample> samples = new ArrayList<>();
//
//        try (CSVReader csvReader = new CSVReader(new InputStreamReader(context.getAssets().open(fileName)))) {
//            String[] row;
//
//            try {
//                while ((row = csvReader.readNext()) != null) {
//                    String label = row[0];
//                    double[] features = new double[row.length - 1];
//                    for (int i = 1; i < row.length; i++) {
//                        features[i - 1] = Double.parseDouble(row[i]);
//                    }
//                    samples.add(new KNNClassifier.Sample(label, features));
//                }
//            } catch (CsvValidationException e) {
//                throw new RuntimeException(e);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return samples;
//    }

    public static List<KNNClassifier.Sample> readSamplesFromCsv(Context context, String fileName) {
        List<KNNClassifier.Sample> samples = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(context.getAssets().open(fileName)))) {
            String[] row;
            try {
                while ((row = csvReader.readNext()) != null) {
                    if (row.length < 2) {
                        // Each row must have at least one label and one feature
                        Log.e("CSV_ERROR", "Skipping malformed row");
                        continue;
                    }
                    String label = row[0];
                    double[] features = new double[row.length - 1];
                    for (int i = 1; i < row.length; i++) {
                        try {
                            // handle empty string
                            if (row[i].isEmpty()) {
                                features[i - 1] = 0.0; // or whatever default value you want to use
                            } else {
                                features[i - 1] = Double.parseDouble(row[i]);
                            }
                        } catch (NumberFormatException e) {
                            Log.e("CSV_ERROR", "Error parsing number in row: " + Arrays.toString(row));
                            throw e;
                        }
                    }
                    samples.add(new KNNClassifier.Sample(label, features));
                }
            } catch (CsvValidationException e) {
                Log.e("CSV_ERROR", "CSV validation error");
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            Log.e("CSV_ERROR", "Error opening or reading file: " + fileName, e);
        }
        return samples;
    }


    public String classify(double[] features) {
        List<SampleDistance> sampleDistances = new ArrayList<>();

        for (Sample sample : samples) {
            double distance = euclideanDistance(features, sample.features);
            sampleDistances.add(new SampleDistance(sample.label, distance));
        }

        Collections.sort(sampleDistances);

        Map<String, Integer> labelCounts = new HashMap<>();
        for (int i = 0; i < k; i++) {
            SampleDistance sampleDistance = sampleDistances.get(i);
            labelCounts.put(sampleDistance.label, labelCounts.getOrDefault(sampleDistance.label, 0) + 1);
        }

        return Collections.max(labelCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static class Sample {
        public final String label;
        public final double[] features;

        public Sample(String label, double[] features) {
            this.label = label;
            this.features = features;
        }
    }

    private static class SampleDistance implements Comparable<SampleDistance> {
        public final String label;
        public final double distance;

        public SampleDistance(String label, double distance) {
            this.label = label;
            this.distance = distance;
        }

        @Override
        public int compareTo(SampleDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}




