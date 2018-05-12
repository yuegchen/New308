package cse308.Thymeleaf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cse308.Thymeleaf.form.RedistrictingForm;
import cse308.Thymeleaf.model.District;
import cse308.Thymeleaf.model.Plan;
import cse308.Thymeleaf.model.Precinct;
import cse308.Thymeleaf.model.State;

public class RedistrictHelpers {

	public List<District> getDistrictsByState(int stateId, EntityManager em){
		return em.find(State.class, stateId).initDistList();
	}
	
	
	public double calculateGoodness(District d, double[] weights) throws IOException {
		double compactness = calculateCompactness(d, weights[0]);
		double population = calculatePopulation(d, weights[1]);
		double politicalFairness = calculatePoliticalFairness(d, weights[2]);
		double contiguity = calculateContiguity(d, weights[3]);
		double racialFairness = calculateRacialFairness(d, weights[4]);
		double totalGoodness = compactness + population + politicalFairness + contiguity + racialFairness;
		return totalGoodness; 
	}

	public boolean checkConstraint(Precinct precinct, District d2) {
		List<District> neighborDistrictList = d2.getNeighborDistricts();
		for (District d : neighborDistrictList) {
			if ((d.getBorderingPrecinctList().contains(precinct))) {
				return true;
			}
		}
		return false;
	}

	public void moveTo(Precinct precinct, District d1, District d2, boolean out) {
		List<Precinct> precinctList = d1.getPrecinctList();
		precinctList.remove(precinct);
		d1.setPrecinctList(precinctList); 
		
		List<Precinct> borderingPrecinctList = d1.getBorderingPrecinctList();
		borderingPrecinctList.remove(precinct);
		d1.setBorderingPrecinctList(borderingPrecinctList);
		
		List<Precinct> precinctList2 = d2.getPrecinctList();
		precinctList2.add(precinct);
		d2.setPrecinctList(precinctList2);
		
		List<Precinct> borderingPrecinctList2 = d2.getBorderingPrecinctList();
		borderingPrecinctList2.remove(precinct);
		d2.setBorderingPrecinctList(borderingPrecinctList2);
		
		if(!out){
			List<Integer> intoPList = d2.getIntoPList();
			intoPList.add(precinct.getPid());
			d2.setIntoPList(intoPList);
		}
		else{
			List<Integer> intoPList = d1.getIntoPList();
			intoPList.remove(precinct.getPid());
			d1.setIntoPList(intoPList);
		}
		move = "{ \"movedPrecincts\" : [[ 'districtId': '"+d2.getDId()+"', \"precinctId\": ["+precinct.getPid()+"]]]}";
		System.out.println("move "+precinct.getPid()+" from "+d1.getDId()+" to "+d2.getDId());
		
	}
	
	public double calculateCompactness(District d, double weight) throws IOException {
		double perimeter = d.getPerimeter();
		double area = d.getArea();
		double r = Math.sqrt(area / Math.PI);
		double equalAreaPerimeter = 2 * Math.PI * r;
		double score = 1 / (perimeter / equalAreaPerimeter);
		return score*weight;
	}

	public double calculatePopulation(District d, double weight) {

		return 0;
	}

	public double calculatePoliticalFairness(District d, double weight) {

		return 0;
	}

	public double calculateContiguity(District d, double weight) {

		return 0;
	}

	public double calculateRacialFairness(District d, double weight) {

		return 0;
	}
}
