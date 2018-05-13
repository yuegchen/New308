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
	
	public double calculateGoodness(District d,District d2, double[] weights) throws IOException {
		double compactness1 = calculateCompactness(d, weights[0]);
		double compactness2 = calculateCompactness(d2, weights[0]);
		double population = calculatePopulation(d,d2, weights[1]);
		double politicalFairness1 = calculatePoliticalFairness(d, weights[2]);
		double politicalFairness2 = calculatePoliticalFairness(d, weights[2]);
		double totalGoodness = compactness1+compactness2 + population*2 + politicalFairness1+politicalFairness2;
		return totalGoodness; 
	}

	public boolean checkConstraint(Precinct precinct, District d2) {
		List<Precinct> neighborPrecinctList = precinct.getNeighborPrecinctList();
		System.err.println("neighborPrecinctList Size" + neighborPrecinctList.size());
		System.err.println("Precinct id: " + precinct.getPid());
		
		for (Precinct p : neighborPrecinctList) {
			if (p != null && p.getCd()==d2.getDId()) {
				System.out.println("true");
				return true;
			}
			
		}
		return false;
	}

	public String moveTo(Precinct precinct, District d1, District d2, boolean out) {
		List<Precinct> precinctList = d1.initPrecList();
		precinctList.remove(precinct);
		d1.setPrecinctList(precinctList); 
		
		List<Precinct> borderingPrecinctList = d1.initBorderingPrecinctList();
		borderingPrecinctList.remove(precinct);
		d1.setBorderingPrecinctList(borderingPrecinctList);
		
		List<Precinct> precinctList2 = d2.initPrecList();
		precinctList2.add(precinct);
		d2.setPrecinctList(precinctList2);
		
		List<Precinct> borderingPrecinctList2 = d2.initBorderingPrecinctList();
		borderingPrecinctList2.add(precinct);
		d2.setBorderingPrecinctList(borderingPrecinctList2);
		
		precinct.setCd(d2.getDId());
				
		if(!out){
			List<Integer> intoPList = d2.getIntoPList();
			System.out.println("intoPList size: "+intoPList.size());
			
			
			intoPList.add(precinct.getPid());
			d2.setIntoPList(intoPList);
		}
		else{
			List<Integer> intoPList = d1.getIntoPList();
			boolean test=intoPList.remove((Integer)precinct.getPid());
			d1.setIntoPList(intoPList);
		}
		return "{ \"movedPrecincts\" : [[ 'districtId': '"+d2.getDId()+"', \"precinctId\": ["+precinct.getPid()+"]]]}";
	}
	
	public double calculateCompactness(District d, double weight) throws IOException {
		double perimeter = d.getPerimeter();
		double area = d.getArea();
		double r = Math.sqrt(area / Math.PI);
		double equalAreaPerimeter = 2 * Math.PI * r;
		double score = 1 / (perimeter / equalAreaPerimeter);
		return score*weight;
	}

	public double calculatePopulation(District d1,District d2, double weight) {
		double popFairness=0;
		int firstPop=d1.getPop();
		int secondPop=d2.getPop();
		popFairness=1-Math.abs(firstPop-secondPop)/(firstPop+secondPop);
		return popFairness*weight;
	}

	public double calculatePoliticalFairness(District d1, double weight) {
		double politicalFairness=0;
		double dem=d1.getDem();
		double rep=d1.getRep();
		politicalFairness=1-Math.abs(dem-rep);
		
		return politicalFairness*weight;
	}

	
}
