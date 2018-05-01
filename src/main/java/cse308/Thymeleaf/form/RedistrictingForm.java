package cse308.Thymeleaf.form;

import cse308.Thymeleaf.model.State;

public class RedistrictingForm {
	private State state;
	private double compactness;
    private double population;
    private double politicalFairness;
    private double contiguity;
    private double racialFairness;
    
    public State getState() {
        return state;
    }
 
    public void setState(State state) {
        this.state= state;
    }
    
    public double getCompactness() {
        return compactness;
    }
 
    public void setCompactness(double compactness) {
        this.compactness= compactness;
    }
    
    public double getPopulation() {
        return population;
    }
 
    public void setPopulation(double population) {
        this.population= population;
    }
    
    public double getPoliticalFairness() {
        return politicalFairness;
    }
 
    public void setPoliticalFairness(double politicalFairness) {
        this.politicalFairness= politicalFairness;
    }
    
    public double getContiguity() {
        return contiguity;
    }
 
    public void setContiguity(double contiguity) {
        this.contiguity= contiguity;
    }
    
    public double getRacialFairness() {
        return racialFairness;
    }
 
    public void setRacialFairness(double racialFairness) {
        this.racialFairness= racialFairness;
    }
}
