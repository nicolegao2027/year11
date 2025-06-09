package com.example.year11;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterActivity extends AppCompatActivity {

    private Spinner yearSpinner;
    private TextView selectedYearText;
    private TextView topCountryText;
    private TextView topEmissionText;
    private TextView totalEmissionText;
    private Button backButton;
    private int selectedYear = 2020;

    // Data from MainActivity
    private Map<String, CountryEmission> countryMap;
    private List<CountryEmission> allCountries;

    // Year range constants
    private static final int MIN_YEAR = 1750;
    private static final int MAX_YEAR = 2020;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        receiveDataFromMain();
        initializeViews();
        setupEventListeners();
        updateDisplayForYear(selectedYear);
    }

    private void receiveDataFromMain() {
        // Get data passed from MainActivity
        Intent intent = getIntent();
        // For now, we'll load data differently since we can't easily pass the full dataset
        // We'll implement a simpler approach
    }

    private void initializeViews() {
        yearSpinner = findViewById(R.id.yearSpinner);
        selectedYearText = findViewById(R.id.selectedYearText);
        topCountryText = findViewById(R.id.topCountryText);
        topEmissionText = findViewById(R.id.topEmissionText);
        totalEmissionText = findViewById(R.id.totalEmissionText);
        backButton = findViewById(R.id.backButton);

        setupYearSpinner();

        selectedYear = MAX_YEAR; // Start at 2020
        updateSelectedYearText(selectedYear);
    }

    private void setupYearSpinner() {
        List<String> years = new ArrayList<>();

        // Add key years and every 5 years to make it more manageable
        // Recent years (every year from 2010-2020)
        for (int year = 2020; year >= 2010; year--) {
            years.add(String.valueOf(year));
        }

        // Every 5 years from 2005 down to 1900
        for (int year = 2005; year >= 1900; year -= 5) {
            years.add(String.valueOf(year));
        }

        // Every 10 years from 1890 down to 1800
        for (int year = 1890; year >= 1800; year -= 10) {
            years.add(String.valueOf(year));
        }

        // Every 25 years from 1775 down to 1750
        for (int year = 1775; year >= 1750; year -= 25) {
            years.add(String.valueOf(year));
        }

        // Create custom adapter with larger dropdown height
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                // Make dropdown items larger and easier to read
                TextView textView = (TextView) view;
                textView.setPadding(32, 24, 32, 24);
                textView.setTextSize(16);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        // Set default selection to 2020 (index 0)
        yearSpinner.setSelection(0);
    }

    private void setupEventListeners() {
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String yearString = (String) parent.getItemAtPosition(position);
                selectedYear = Integer.parseInt(yearString);
                updateSelectedYearText(selectedYear);
                updateDisplayForYear(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        backButton.setOnClickListener(v -> {
            finish(); // Return to MainActivity
        });
    }

    private void updateSelectedYearText(int year) {
        selectedYearText.setText("Selected Year: " + year);
    }

    private void updateDisplayForYear(int year) {
        // Since we can't easily pass all data from MainActivity,
        // we'll load CSV data here or use a simpler approach
        loadDataAndFindTopCountry(year);
    }

    private void loadDataAndFindTopCountry(int year) {
        // Show loading state
        topCountryText.setText("Loading...");
        topEmissionText.setText("Loading...");
        totalEmissionText.setText("Loading...");

        // Load CSV data to find top country for this year
        CSVParser csvParser = new CSVParser();
        csvParser.loadCSVData(this, new CSVParser.DataLoadListener() {
            @Override
            public void onDataLoaded(Map<String, CountryEmission> countries) {
                findTopCountryForYear(countries, year);
            }

            @Override
            public void onLoadProgress(String message) {
                topCountryText.setText("Loading: " + message);
            }

            @Override
            public void onLoadError(String error) {
                topCountryText.setText("Error loading data");
                topEmissionText.setText("Please try again");
                totalEmissionText.setText("");
                Toast.makeText(FilterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findTopCountryForYear(Map<String, CountryEmission> countries, int year) {
        String topCountryName = "";
        double topEmission = 0.0;
        double totalWorldEmission = 0.0;
        int countriesWithData = 0;

        // Find country with highest emission for this year
        for (CountryEmission country : countries.values()) {
            double emission = country.getEmissionsForYear(year);

            if (emission > 0) {
                totalWorldEmission += emission;
                countriesWithData++;

                if (emission > topEmission) {
                    topEmission = emission;
                    topCountryName = country.getCountryName();
                }
            }
        }

        // Update display
        if (!topCountryName.isEmpty()) {
            topCountryText.setText("Highest Emitter: " + topCountryName);
            topEmissionText.setText(String.format("CO₂ Emissions: %.1f Mt", topEmission));
            totalEmissionText.setText(String.format("Total World Emissions: %.1f Mt\n(%d countries with data)",
                    totalWorldEmission, countriesWithData));
        } else {
            topCountryText.setText("No data available for " + year);
            topEmissionText.setText("No emissions recorded");
            totalEmissionText.setText("Total World Emissions: 0.0 Mt");
        }
    }
}