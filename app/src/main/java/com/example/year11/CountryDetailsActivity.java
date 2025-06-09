package com.example.year11;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import java.util.Map;
import java.util.TreeMap;

public class CountryDetailsActivity extends AppCompatActivity {

    private TextView countryNameText;
    private TextView populationText;
    private TextView areaText;
    private TextView densityText;
    private TextView landmassPercentageText;
    private TextView totalEmissionsText;
    private TextView avgEmissionsPerCapitaText;
    private TextView highestEmissionYearText;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);

        Log.d("CountryDetails", "onCreate started");

        try {
            initializeViews();
            Log.d("CountryDetails", "Views initialized successfully");

            displayCountryData();
            Log.d("CountryDetails", "Country data displayed");

            setupEventListeners();
            Log.d("CountryDetails", "Event listeners set up");

        } catch (Exception e) {
            Log.e("CountryDetails", "Error in onCreate", e);
            showErrorMessage("Error in onCreate: " + e.getMessage());
        }
    }

    private void initializeViews() {
        try {
            countryNameText = findViewById(R.id.countryNameText);
            populationText = findViewById(R.id.populationText);
            areaText = findViewById(R.id.areaText);
            densityText = findViewById(R.id.densityText);
            landmassPercentageText = findViewById(R.id.landmassPercentageText);
            totalEmissionsText = findViewById(R.id.totalEmissionsText);
            avgEmissionsPerCapitaText = findViewById(R.id.avgEmissionsPerCapitaText);
            highestEmissionYearText = findViewById(R.id.highestEmissionYearText);
            backButton = findViewById(R.id.backButton);

            // Check if all views were found
            if (countryNameText == null) Log.e("CountryDetails", "countryNameText is null");
            if (populationText == null) Log.e("CountryDetails", "populationText is null");
            if (areaText == null) Log.e("CountryDetails", "areaText is null");
            if (densityText == null) Log.e("CountryDetails", "densityText is null");
            if (landmassPercentageText == null) Log.e("CountryDetails", "landmassPercentageText is null");
            if (totalEmissionsText == null) Log.e("CountryDetails", "totalEmissionsText is null");
            if (avgEmissionsPerCapitaText == null) Log.e("CountryDetails", "avgEmissionsPerCapitaText is null");
            if (highestEmissionYearText == null) Log.e("CountryDetails", "highestEmissionYearText is null");
            if (backButton == null) Log.e("CountryDetails", "backButton is null");

        } catch (Exception e) {
            Log.e("CountryDetails", "Error initializing views", e);
            throw e;
        }
    }

    private void setupEventListeners() {
        try {
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            Log.e("CountryDetails", "Error setting up event listeners", e);
        }
    }

    private void displayCountryData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                Log.e("CountryDetails", "Intent is null");
                showErrorMessage("No data received");
                return;
            }

            // Get basic data
            String countryName = intent.getStringExtra("COUNTRY_NAME");
            long population = intent.getLongExtra("POPULATION", -1);
            double area = intent.getDoubleExtra("AREA", -1.0);
            double density = intent.getDoubleExtra("DENSITY", -1.0);
            double percentage = intent.getDoubleExtra("PERCENTAGE", -1.0);

            Log.d("CountryDetails", "=== RECEIVED DATA ===");
            Log.d("CountryDetails", "Country: " + countryName);
            Log.d("CountryDetails", "Population: " + population);
            Log.d("CountryDetails", "Area: " + area);
            Log.d("CountryDetails", "Density: " + density);
            Log.d("CountryDetails", "Percentage: " + percentage);

            // Display basic information
            displayBasicInfo(countryName, population, area, density, percentage);

            // Get emissions data
            Map<Integer, Double> emissionsData = extractEmissionsData(intent);
            Log.d("CountryDetails", "Extracted " + emissionsData.size() + " years of emissions data");

            // Calculate and display statistics
            calculateAndDisplayStatistics(emissionsData, population);

        } catch (Exception e) {
            Log.e("CountryDetails", "Error in displayCountryData", e);
            showErrorMessage("Error displaying data: " + e.getMessage());
        }
    }

    private void displayBasicInfo(String countryName, long population, double area, double density, double percentage) {
        try {
            // Country name
            if (countryNameText != null) {
                countryNameText.setText(countryName != null ? countryName : "Unknown Country");
            }

            // Population
            if (populationText != null) {
                if (population > 0) {
                    populationText.setText(String.format("Population: %,d", population));
                } else {
                    populationText.setText("Population: Data not available");
                }
            }

            // Area
            if (areaText != null) {
                if (area > 0) {
                    areaText.setText(String.format("Area: %,.0f km²", area));
                } else if (population > 0 && density > 0) {
                    double calculatedArea = population / density;
                    areaText.setText(String.format("Area: %,.0f km² (calculated)", calculatedArea));
                } else {
                    areaText.setText("Area: Data not available");
                }
            }

            // Density
            if (densityText != null) {
                if (density > 0) {
                    densityText.setText(String.format("Density: %.1f people/km²", density));
                } else {
                    densityText.setText("Density: Data not available");
                }
            }

            // Percentage
            if (landmassPercentageText != null) {
                if (percentage > 0) {
                    landmassPercentageText.setText(String.format("%% of World Landmass: %.2f%%", percentage));
                } else {
                    landmassPercentageText.setText("% of World Landmass: Data not available");
                }
            }

            Log.d("CountryDetails", "Basic info displayed successfully");

        } catch (Exception e) {
            Log.e("CountryDetails", "Error displaying basic info", e);
        }
    }

    private Map<Integer, Double> extractEmissionsData(Intent intent) {
        Map<Integer, Double> emissionsData = new TreeMap<>();

        try {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.w("CountryDetails", "No extras in intent");
                return emissionsData;
            }

            Log.d("CountryDetails", "=== ALL INTENT EXTRAS ===");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d("CountryDetails", key + " = " + value + " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");

                if (key.startsWith("EMISSION_")) {
                    try {
                        String yearStr = key.substring(9); // Remove "EMISSION_" prefix
                        int year = Integer.parseInt(yearStr);
                        double emission = extras.getDouble(key, -1.0);

                        if (emission >= 0) {
                            emissionsData.put(year, emission);
                            Log.d("CountryDetails", "Added emission: " + year + " -> " + emission);
                        }
                    } catch (Exception e) {
                        Log.w("CountryDetails", "Error parsing emission key: " + key, e);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("CountryDetails", "Error extracting emissions data", e);
        }

        return emissionsData;
    }

    private void calculateAndDisplayStatistics(Map<Integer, Double> emissionsData, long population) {
        try {
            if (emissionsData.isEmpty()) {
                Log.w("CountryDetails", "No emissions data available for statistics");
                showNoDataState();
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

                // Total emissions
                totalEmissions += emission;

                // Highest emission year
                if (emission > highestEmission) {
                    highestEmission = emission;
                    highestEmissionYear = year;
                }

                // Most recent year
                if (year > mostRecentYear) {
                    mostRecentYear = year;
                    recentEmission = emission;
                }
            }

            Log.d("CountryDetails", "=== CALCULATED STATISTICS ===");
            Log.d("CountryDetails", "Total emissions: " + totalEmissions);
            Log.d("CountryDetails", "Highest emission: " + highestEmission + " in " + highestEmissionYear);
            Log.d("CountryDetails", "Recent emission: " + recentEmission + " in " + mostRecentYear);

            // Display statistics
            displayStatistics(totalEmissions, highestEmission, highestEmissionYear,
                    recentEmission, mostRecentYear, population);

        } catch (Exception e) {
            Log.e("CountryDetails", "Error calculating statistics", e);
            showErrorState();
        }
    }

    private void displayStatistics(double totalEmissions, double highestEmission, int highestEmissionYear,
                                   double recentEmission, int mostRecentYear, long population) {
        try {
            // Total emissions
            if (totalEmissionsText != null) {
                totalEmissionsText.setText(String.format("Total CO₂ Emissions (All Years): %.1f Mt", totalEmissions));
            }

            // Per capita emissions
            if (avgEmissionsPerCapitaText != null) {
                if (population > 0 && recentEmission > 0) {
                    double emissionsPerCapita = (recentEmission * 1000000) / population;
                    avgEmissionsPerCapitaText.setText(String.format("Emissions per Capita (%d): %.2f tons/person",
                            mostRecentYear, emissionsPerCapita));
                } else {
                    avgEmissionsPerCapitaText.setText("Emissions per Capita: Data not available");
                }
            }

            // Highest emission year
            if (highestEmissionYearText != null) {
                if (highestEmissionYear > 0 && highestEmission > 0) {
                    highestEmissionYearText.setText(String.format("Year with Highest Emissions: %d (%.1f Mt)",
                            highestEmissionYear, highestEmission));
                } else {
                    highestEmissionYearText.setText("Year with Highest Emissions: No data available");
                }
            }

            Log.d("CountryDetails", "Statistics displayed successfully");

        } catch (Exception e) {
            Log.e("CountryDetails", "Error displaying statistics", e);
        }
    }

    private void showNoDataState() {
        safeSetText(totalEmissionsText, "Total CO₂ Emissions: No data available");
        safeSetText(avgEmissionsPerCapitaText, "Emissions per Capita: No data available");
        safeSetText(highestEmissionYearText, "Year with Highest Emissions: No data available");
    }

    private void showErrorState() {
        safeSetText(totalEmissionsText, "Total CO₂ Emissions: Error loading data");
        safeSetText(avgEmissionsPerCapitaText, "Emissions per Capita: Error loading data");
        safeSetText(highestEmissionYearText, "Year with Highest Emissions: Error loading data");
    }

    private void showErrorMessage(String message) {
        safeSetText(countryNameText, "Error Loading Data");
        Log.e("CountryDetails", "Error message: " + message);
        showErrorState();
    }

    private void safeSetText(TextView textView, String text) {
        try {
            if (textView != null) {
                textView.setText(text);
            }
        } catch (Exception e) {
            Log.e("CountryDetails", "Error setting text: " + text, e);
        }
    }
}