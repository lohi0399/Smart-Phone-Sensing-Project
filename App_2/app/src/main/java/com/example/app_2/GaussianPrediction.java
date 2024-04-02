package com.example.app_2;

import android.content.Context;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GaussianPrediction {

    private static final double CONVERGENCE_THRESHOLD = 0.95;
    private static final int MAX_ITERATIONS = 50;
    private Map<String, Map<String, List<double[]>>> cellData; // Changed to Map<String, List<double[]>>
    private boolean isDataLoadedSuccessfully = false;

    private Context context;

    public GaussianPrediction(Context context) {
        this.context = context;
    }

    public void loadCellData() {
        cellData = new HashMap<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(context.getAssets().open("bayesian_2.csv")))) {
            String[] values;
            reader.readNext(); // Skip header line
            while (((values = reader.readNext()) != null)) {
                String cellId = values[0].trim();
                String bssid = values[1].trim();
                double mean = Double.parseDouble(values[3].trim());
                double stdDev = Double.parseDouble(values[4].trim());

                // Create a list for the data and add it to the map
                Map<String, List<double[]>> cellEntry = cellData.getOrDefault(cellId, new HashMap<>());
                List<double[]> bssidData = cellEntry.getOrDefault(bssid, new ArrayList<>());
                bssidData.add(new double[]{mean, stdDev});
                cellEntry.put(bssid, bssidData);
                cellData.put(cellId, cellEntry);
            }
        } catch (IOException | CsvValidationException e) {
            isDataLoadedSuccessfully = false;
            return;
        }
        isDataLoadedSuccessfully = true;
    }

    public String inferCell(Map<String, Double> bssid_rssi, boolean worstCase) {
        double prior = 1.0 / cellData.size();
        Map<String, Double> posteriorProbs = null;
//        for (int i = 0; i < MAX_ITERATIONS; i++) {
        posteriorProbs = new HashMap<>();
        for (Map.Entry<String, Map<String, List<double[]>>> cellEntry : cellData.entrySet()) {
            String cell = cellEntry.getKey();
            Map<String, List<double[]>> data = cellEntry.getValue();
            double prob = Math.log(prior);
            for (Map.Entry<String, Double> rssEntry : bssid_rssi.entrySet()) {
                String bssid = rssEntry.getKey();
                double rssi = rssEntry.getValue();
                if (data.containsKey(bssid)) {
                    double probForBssid = Double.NEGATIVE_INFINITY;
                    for (double[] params : data.get(bssid)) {
                        if (params[1] > 0) { // Check standard deviation
                            NormalDistribution dist = new NormalDistribution(params[0], params[1]);
                            probForBssid = Math.max(probForBssid, Math.log(dist.density(rssi)));
                        }
                    }
                    prob += probForBssid;
                }
            }
            posteriorProbs.put(cell, prob);
        }
        double sumProbs = posteriorProbs.values().stream().mapToDouble(Math::exp).sum();
        posteriorProbs.replaceAll((k, v) -> Math.exp(v) / sumProbs);
        Optional<Map.Entry<String, Double>> maxEntry = posteriorProbs.entrySet().stream().max(Map.Entry.comparingByValue());
        if (maxEntry.isPresent() && maxEntry.get().getValue() >= CONVERGENCE_THRESHOLD) {
            return maxEntry.get().getKey();
        }
//            prior = 1.0 / cellData.size();
//        }

        if (worstCase) {
            Optional<Map.Entry<String, Double>> maxEntryWC = posteriorProbs.entrySet().stream().max(Map.Entry.comparingByValue());
            if (maxEntryWC.isPresent()) {
                return maxEntryWC.get().getKey();
            }
        }

        return null;
    }

    public boolean isGaussianPredictionReady() {
        return isDataLoadedSuccessfully;
    }
}

