package cse308.Thymeleaf.controller;

import java.io.IOException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cse308.Thymeleaf.form.RedistrictingForm;
import cse308.Thymeleaf.form.RegisterForm;
import cse308.Thymeleaf.model.*;

@Controller
public class RedistrictingController {
	private static State state;
	private static double[] weights;

	@RequestMapping(value = { "/redistrict" }, method = RequestMethod.POST)
	public String startAlgo(Model model, //
			@ModelAttribute("redistrictingForm") RedistrictingForm redistrictingForm) throws IOException {
		
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		state = entitymanager.find(State.class, 27);
		System.out.println(state.getStateId());
		
		List<District> dList = state.initDistList();
		
		for(District d:dList){
			List<Precinct> borderPrecinctList = d.initBorderingPrecinctList();
			List<District> neighborDistrictList = d.getNeighborDistricts();
			for(District to:neighborDistrictList){
				tryMove(borderPrecinctList,d,to);
			}
		}
		
		Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state, "test");
		model.addAttribute("plan", plan);
		System.out.println("Success!!");
		return "index";
	}

	private void tryMove(List<Precinct> borderPrecinctList,District from, District to) throws IOException {
		double originalScore=calculateGoodness(from)+calculateGoodness(to);
		for (Precinct precinct : borderPrecinctList) {
			if(checkConstraint(precinct,to))
				moveTo(precinct, from, to, false);
			else
				continue;
			double newScore=calculateGoodness(from)+calculateGoodness(to);
			if ( newScore> originalScore) 
				originalScore=newScore;
			else
				moveTo(precinct,to,from, true);
		}
		
	}
	
	public double calculateGoodness(District d) throws IOException {
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
			if (Arrays.asList(d.getBorderingPrecinctList()).contains(precinct)) {
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
			List<Integer> intoPList = d2.getIntoPList();
			intoPList.remove(precinct.getPid());
			d2.setIntoPList(intoPList);
		}
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
