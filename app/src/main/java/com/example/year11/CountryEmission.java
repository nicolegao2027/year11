package com.example.year11;

import java.util.HashMap;
import java.util.Map;

public class CountryEmission {
    private String countryName;
    private Map<Integer, Double> co2Emissions;
    private long population;
    private double area;
    private double density;
    private double percentageOfWorld;

    public CountryEmission(String countryName) {
        this.countryName = countryName;
        this.co2Emissions = new HashMap<>();
        this.population = 0;
        this.area = 0.0;
        this.density = 0.0;
        this.percentageOfWorld = 0.0;
    }

    // Getters and Setters
    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Map<Integer, Double> getCo2Emissions() {
        return co2Emissions;
    }

    public void addCo2Emission(int year, double emission) {
        this.co2Emissions.put(year, emission);
    }

    public double getEmissionsForYear(int year) {
        Double emission = co2Emissions.get(year);
        return emission != null ? emission : 0.0;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getPercentageOfWorld() {
        return percentageOfWorld;
    }

    public void setPercentageOfWorld(double percentageOfWorld) {
        this.percentageOfWorld = percentageOfWorld;
    }

    @Override
    public String toString() {
        return "CountryEmission{" +
                "countryName='" + countryName + '\'' +
                ", population=" + population +
                ", area=" + area +
                ", density=" + density +
                ", percentageOfWorld=" + percentageOfWorld +
                ", yearsOfData=" + co2Emissions.size() +
                '}';
    }
}