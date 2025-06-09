package com.example.year11;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Constants
    private static final int FILTER_REQUEST_CODE = 1001;

    // Data structures
    private Map<String, CountryEmission> countryMap;
    private List<CountryEmission> allCountries;
    private List<CountryEmission> top10Countries;

    // UI Components
    private ListView top10ListView;
    private EditText searchEditText;
    private Button filterByYearButton;
    private TextView statusText;
    private ArrayAdapter<String> top10Adapter;

    // Data processing
    private CSVParser csvParser;
    private int currentFilterYear = 2020;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadCSVDataInBackground();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        filterByYearButton = findViewById(R.id.filterByYearButton);
        top10ListView = findViewById(R.id.top10ListView);
        statusText = findViewById(R.id.statusText);

        countryMap = new HashMap<>();
        allCountries = new ArrayList<>();
        top10Countries = new ArrayList<>();

        List<String> displayList = new ArrayList<>();
        top10Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        top10ListView.setAdapter(top10Adapter);

        setupEventListeners();

        // Show loading message
        updateStatus("Loading CO₂ emissions data...");
    }

    private void setupEventListeners() {
        // Search functionality
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String searchText = searchEditText.getText().toString().trim();
                if (searchText.length() > 0) {
                    searchCountryForDetails(searchText);
                } else {
                    Toast.makeText(this, "Please enter a country name", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                String searchText = searchEditText.getText().toString().trim();
                if (searchText.length() > 0) {
                    searchCountryForDetails(searchText);
                } else {
                    Toast.makeText(this, "Please enter a country name", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // Filter button functionality
        filterByYearButton.setOnClickListener(v -> {
            Intent filterIntent = new Intent(this, FilterActivity.class);
            startActivity(filterIntent);
        });

        // Top 10 list click functionality - NOW OPENS FirstActivity
        top10ListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < top10Countries.size()) {
                CountryEmission selectedCountry = top10Countries.get(position);
                openFirstActivityScreen(selectedCountry); // Changed from openCountryDetailsScreen
            }
        });
    }

    // FirstActivity
    private void openFirstActivityScreen(CountryEmission country) {
        try {
            Log.d("MainActivity", "=== OPENING FIRST ACTIVITY ===");
            Log.d("MainActivity", "Country: " + country.getCountryName());
            Log.d("MainActivity", "Population: " + country.getPopulation());

            Intent intent = new Intent(this, FirstActivity.class);

            intent.putExtra("COUNTRY_NAME", country.getCountryName());
            intent.putExtra("POPULATION", country.getPopulation());
            intent.putExtra("AREA", country.getArea());
            intent.putExtra("DENSITY", country.getDensity());
            intent.putExtra("PERCENTAGE", country.getPercentageOfWorld());

            Map<Integer, Double> emissions = country.getCo2Emissions();
            Log.d("MainActivity", "Adding " + emissions.size() + " years of emission data");

            for (Map.Entry<Integer, Double> entry : emissions.entrySet()) {
                String key = "EMISSION_" + entry.getKey();
                Double value = entry.getValue();
                intent.putExtra(key, value);
                Log.d("MainActivity", "Added: " + key + " = " + value);
            }

            Log.d("MainActivity", "Starting FirstActivity");
            startActivity(intent);

        } catch (Exception e) {
            Log.e("MainActivity", "Error opening first activity", e);
            Toast.makeText(this, "Error opening statistics for " + country.getCountryName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCSVDataInBackground() {
        updateStatus("Loading CSV data...");

        csvParser = new CSVParser();
        csvParser.loadCSVData(this, new CSVParser.DataLoadListener() {
            @Override
            public void onDataLoaded(Map<String, CountryEmission> countries) {
                // Load real data (only countries with 2020 data)
                countryMap.clear();
                countryMap.putAll(countries);
                allCountries.clear();
                allCountries.addAll(countryMap.values());

                calculateTop10Polluters();
                updateTop10Display();
                updateStatus("Data loaded! " + countries.size() + " countries with 2020 data. Historical range: 1750-2020.");
            }

            @Override
            public void onLoadProgress(String message) {
                updateStatus("Loading: " + message);
            }

            @Override
            public void onLoadError(String error) {
                updateStatus("Error loading data: " + error + ". Please check CSV file.");
                Log.e("MainActivity", "CSV load error: " + error);

                // Show empty state
                top10Adapter.clear();
                top10Adapter.add("No data available");
                top10Adapter.notifyDataSetChanged();
            }
        });
    }

    private void calculateTop10Polluters() {
        top10Countries.clear();

        List<CountryEmission> countriesWithCurrentYearData = new ArrayList<>();
        for (CountryEmission country : allCountries) {
            double emissions = country.getEmissionsForYear(currentFilterYear);
            if (emissions > 0) {
                countriesWithCurrentYearData.add(country);
            }
        }

        Collections.sort(countriesWithCurrentYearData, new Comparator<CountryEmission>() {
            @Override
            public int compare(CountryEmission c1, CountryEmission c2) {
                double emissions1 = c1.getEmissionsForYear(currentFilterYear);
                double emissions2 = c2.getEmissionsForYear(currentFilterYear);
                return Double.compare(emissions2, emissions1);
            }
        });

        for (int i = 0; i < Math.min(10, countriesWithCurrentYearData.size()); i++) {
            top10Countries.add(countriesWithCurrentYearData.get(i));
        }
    }

    private void updateTop10Display() {
        List<String> displayList = new ArrayList<>();

        if (top10Countries.isEmpty()) {
            displayList.add("No data available for " + currentFilterYear);
        } else {
            for (int i = 0; i < top10Countries.size(); i++) {
                CountryEmission country = top10Countries.get(i);
                double emissions = country.getEmissionsForYear(currentFilterYear);

                String displayText = String.format("%d. %s\nCO₂ (%d): %.1f Mt",
                        i + 1, country.getCountryName(), currentFilterYear, emissions);
                displayList.add(displayText);
            }
        }

        top10Adapter.clear();
        top10Adapter.addAll(displayList);
        top10Adapter.notifyDataSetChanged();
    }

    private void searchCountryForDetails(String searchText) {
        if (countryMap.isEmpty()) {
            Toast.makeText(this, "Data still loading, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        CountryEmission exactMatch = countryMap.get(searchText.toLowerCase());
        if (exactMatch != null) {
            openCountryDetailsScreen(exactMatch);
            return;
        }

        for (CountryEmission country : allCountries) {
            if (country.getCountryName().toLowerCase().contains(searchText.toLowerCase())) {
                openCountryDetailsScreen(country);
                return;
            }
        }

        Toast.makeText(this, "Country '" + searchText + "' not found", Toast.LENGTH_SHORT).show();
    }

    private void openCountryDetailsScreen(CountryEmission country) {
        try {
            Log.d("MainActivity", "=== OPENING COUNTRY DETAILS ===");
            Log.d("MainActivity", "Country: " + country.getCountryName());

            Intent intent = new Intent(this, CountryDetailsActivity.class);

            intent.putExtra("COUNTRY_NAME", country.getCountryName());
            intent.putExtra("POPULATION", country.getPopulation());
            intent.putExtra("AREA", country.getArea());
            intent.putExtra("DENSITY", country.getDensity());
            intent.putExtra("PERCENTAGE", country.getPercentageOfWorld());

            Map<Integer, Double> emissions = country.getCo2Emissions();
            for (Map.Entry<Integer, Double> entry : emissions.entrySet()) {
                String key = "EMISSION_" + entry.getKey();
                Double value = entry.getValue();
                intent.putExtra(key, value);
            }

            startActivity(intent);
            searchEditText.setText("");

        } catch (Exception e) {
            Log.e("MainActivity", "Error opening country details", e);
            Toast.makeText(this, "Error opening details for " + country.getCountryName(), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateStatus(String message) {
        statusText.setText(message);
        Log.d("MainActivity", message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (csvParser != null) {
            csvParser.shutdown();
        }
    }
}