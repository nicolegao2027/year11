package com.example.year11;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import java.util.Map;
import java.util.TreeMap;

public class FirstActivity extends AppCompatActivity {

    private TextView countryNameText;
    private TextView totalEmissionsText;
    private TextView avgEmissionsPerCapitaText;
    private TextView highestEmissionYearText;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        initializeViews();
        displayCountryStatistics();
        setupEventListeners();
    }

    private void initializeViews() {
        countryNameText = findViewById(R.id.countryNameText);
        totalEmissionsText = findViewById(R.id.totalEmissionsText);
        avgEmissionsPerCapitaText = findViewById(R.id.avgEmissionsPerCapitaText);
        highestEmissionYearText = findViewById(R.id.highestEmissionYearText);
        backButton = findViewById(R.id.backButton);
    }

    private void setupEventListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void displayCountryStatistics() {
        Intent intent = getIntent();

        try {
            // Get basic data
            String countryName = intent.getStringExtra("COUNTRY_NAME");
            long population = intent.getLongExtra("POPULATION", 0);

            countryNameText.setText(countryName != null ? countryName : "Unknown Country");

            // Get emissions data
            Map<Integer, Double> emissionsData = new TreeMap<>();

            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    if (key.startsWith("EMISSION_")) {
                        try {
                            int year = Integer.parseInt(key.substring(9));
                            double emission = extras.getDouble(key);
                            if (emission >= 0) {
                                emissionsData.put(year, emission);
                            }
                        } catch (Exception e) {
                            Log.d("FirstActivity", "Error parsing emission data: " + key);
                        }
                    }
                }
            }

            calculateAndDisplayStatistics(emissionsData, population);

        } catch (Exception e) {
            Log.e("FirstActivity", "Error displaying country data", e);
            countryNameText.setText("Error Loading Country Data");
        }
    }

    private void calculateAndDisplayStatistics(Map<Integer, Double> emissionsData, long population) {
        if (emissionsData.isEmpty()) {
            totalEmissionsText.setText("Total CO₂ Emissions: No data available");
            avgEmissionsPerCapitaText.setText("Average Emissions per Capita: No data available");
            highestEmissionYearText.setText("Year with Highest Emissions: No data available");
            return;
        }

        // Calculate statistics
        double totalEmissions = 0.0;
        int highestEmissionYear = 0;
        double highestEmission = 0.0;
        int mostRecentYear = 0;
        double recentEmission = 0.0;

        for (Map.Entry<Integer, Double> entry : emissionsData.entrySet()) {
            int year = entry.getKey();
            double emission = entry.getValue();

            totalEmissions += emission;

            if (emission > highestEmission) {
                highestEmission = emission;
                highestEmissionYear = year;
            }

            if (year > mostRecentYear) {
                mostRecentYear = year;
                recentEmission = emission;
            }
        }

        // Display total emissions
        totalEmissionsText.setText(String.format("Total CO₂ Emissions (All Years):\n%.1f Mt", totalEmissions));

        // Calculate and display average emissions per capita
        if (population > 0 && recentEmission > 0) {
            double emissionsPerCapita = (recentEmission * 1000000) / population;
            avgEmissionsPerCapitaText.setText(String.format("Emissions per Capita (%d):\n%.2f tons/person",
                    mostRecentYear, emissionsPerCapita));
        } else {
            avgEmissionsPerCapitaText.setText("Emissions per Capita:\nData not available");
        }

        // Display year with highest emissions
        if (highestEmissionYear > 0 && highestEmission > 0) {
            highestEmissionYearText.setText(String.format("Year with Highest Emissions:\n%d (%.1f Mt)",
                    highestEmissionYear, highestEmission));
        } else {
            highestEmissionYearText.setText("Year with Highest Emissions:\nNo data available");
        }
    }
}