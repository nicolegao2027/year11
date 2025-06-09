package com.example.year11;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSVParser {

    public interface DataLoadListener {
        void onDataLoaded(Map<String, CountryEmission> countries);
        void onLoadProgress(String message);
        void onLoadError(String error);
    }

    private ExecutorService executor;
    private Handler mainHandler;

    // Year range constants
    private static final int MIN_YEAR = 1750;
    private static final int MAX_YEAR = 2020;
    private static final int MAIN_DISPLAY_YEAR = 2020;

    public CSVParser() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadCSVData(Context context, DataLoadListener listener) {
        executor.execute(() -> {
            try {
                Map<String, CountryEmission> countries = parseCSVFile(context, listener);

                // Filter to only include countries with 2020 data for main display
                Map<String, CountryEmission> countries2020 = filterCountriesWith2020Data(countries);

                mainHandler.post(() -> listener.onDataLoaded(countries2020));
            } catch (Exception e) {
                Log.e("CSVParser", "Error loading CSV", e);
                mainHandler.post(() -> listener.onLoadError(e.getMessage()));
            }
        });
    }

    private Map<String, CountryEmission> filterCountriesWith2020Data(Map<String, CountryEmission> allCountries) {
        Map<String, CountryEmission> filtered = new HashMap<>();

        for (Map.Entry<String, CountryEmission> entry : allCountries.entrySet()) {
            CountryEmission country = entry.getValue();

            // Only include countries that have 2020 emissions data > 0
            if (country.getEmissionsForYear(MAIN_DISPLAY_YEAR) > 0) {
                filtered.put(entry.getKey(), country);
            }
        }

        Log.d("CSVParser", "Filtered from " + allCountries.size() + " to " + filtered.size() + " countries with 2020 data");
        return filtered;
    }

    private Map<String, CountryEmission> parseCSVFile(Context context, DataLoadListener listener) throws Exception {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("co2_emission_by_countries.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Map<String, CountryEmission> countryMap = new HashMap<>();
        String line;
        int lineCount = 0;
        int processedLines = 0;
        int batchSize = 1000;
        boolean isFirstLine = true;

        mainHandler.post(() -> listener.onLoadProgress("Starting CSV processing..."));

        while ((line = reader.readLine()) != null) {
            lineCount++;

            if (isFirstLine) {
                isFirstLine = false;
                Log.d("CSVParser", "Header: " + line);
                continue;
            }

            // Process line
            if (parseCSVLine(line, countryMap)) {
                processedLines++;
            }

            // Update progress
            if (lineCount % batchSize == 0) {
                final int currentLine = lineCount;
                final int processed = processedLines;
                final int countries = countryMap.size();

                mainHandler.post(() ->
                        listener.onLoadProgress("Processed " + processed + " records from " + currentLine + " lines (" + countries + " countries)"));

                // Small pause to prevent UI blocking
                Thread.sleep(50);
            }

            // Process more data but with reasonable limit
            if (lineCount > 100000) {
                Log.d("CSVParser", "Limited to 100k lines for performance");
                break;
            }
        }

        reader.close();
        inputStream.close();

        Log.d("CSVParser", "Parsing complete: " + processedLines + " records processed, " + countryMap.size() + " countries total");

        return countryMap;
    }

    private boolean parseCSVLine(String line, Map<String, CountryEmission> countryMap) {
        try {
            // Handle CSV with commas in quoted fields
            String[] values = parseCSVLineWithQuotes(line);

            // Based on your format: Country,Code,Calling Code,Year,CO2 emission (Tons),Population(2022),Area,% of World,Density(km2)
            if (values.length < 9) {
                Log.d("CSVParser", "Skipping line with insufficient columns: " + values.length);
                return false;
            }

            String countryName = values[0].trim();
            String yearStr = values[3].trim();
            String emissionStr = values[4].trim();
            String populationStr = values[5].trim();
            String areaStr = values[6].trim(); // Area column
            String percentageStr = values[7].trim();
            String densityStr = values[8].trim();

            if (countryName.isEmpty() || yearStr.isEmpty() || emissionStr.isEmpty()) {
                return false;
            }

            int year = Integer.parseInt(yearStr);

            // Only process data within our valid year range
            if (year < MIN_YEAR || year > MAX_YEAR) {
                return false;
            }

            double emission = Double.parseDouble(emissionStr);

            // Don't skip zero emissions - they're valid data points
            // if (emission < 0.01) return false;

            String countryKey = countryName.toLowerCase();
            CountryEmission country = countryMap.get(countryKey);

            if (country == null) {
                country = new CountryEmission(countryName);

                // Add other data safely
                try {
                    // Population (2022 data, same for all years)
                    if (!populationStr.isEmpty() && !populationStr.equals("0")) {
                        long population = Long.parseLong(populationStr);
                        if (population > 0) {
                            country.setPopulation(population);
                        }
                    }

                    // Area (in km²)
                    if (!areaStr.isEmpty() && !areaStr.equals("0")) {
                        try {
                            double area = Double.parseDouble(areaStr);
                            if (area > 0) {
                                country.setArea(area);
                            }
                        } catch (NumberFormatException e) {
                            Log.d("CSVParser", "Error parsing area for " + countryName + ": " + areaStr);
                        }
                    }

                    // Percentage of world (remove % sign)
                    if (!percentageStr.isEmpty() && !percentageStr.equals("0.00%") && percentageStr.contains("%")) {
                        String cleanPercent = percentageStr.replace("%", "").trim();
                        try {
                            double percentage = Double.parseDouble(cleanPercent);
                            if (percentage > 0) {
                                country.setPercentageOfWorld(percentage);
                            }
                        } catch (NumberFormatException e) {
                            Log.d("CSVParser", "Error parsing percentage for " + countryName + ": " + percentageStr);
                        }
                    }

                    // Density (extract number from "63/km²" format)
                    if (!densityStr.isEmpty() && densityStr.contains("/km")) {
                        try {
                            String densityNumber = densityStr.split("/")[0].trim();
                            double density = Double.parseDouble(densityNumber);
                            if (density > 0) {
                                country.setDensity(density);
                            }
                        } catch (Exception e) {
                            Log.d("CSVParser", "Error parsing density for " + countryName + ": " + densityStr);
                        }
                    }

                } catch (Exception e) {
                    Log.d("CSVParser", "Error parsing additional data for " + countryName + ": " + e.getMessage());
                    // Continue with just emission data
                }

                countryMap.put(countryKey, country);
            }

            country.addCo2Emission(year, emission);
            return true;

        } catch (Exception e) {
            Log.d("CSVParser", "Error parsing line: " + e.getMessage());
            return false;
        }
    }

    // Handle CSV lines that might have quoted fields with commas
    private String[] parseCSVLineWithQuotes(String line) {
        // Simple CSV parser - split by comma but handle basic cases
        return line.split(",");
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}