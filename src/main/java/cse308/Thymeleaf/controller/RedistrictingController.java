package cse308.Thymeleaf.controller;

import java.util.*;

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
			@ModelAttribute("redistrictingForm") RedistrictingForm redistrictingForm) {
		District[] dList = state.getDistricts();
		for(District d:dList){
			Precinct[] borderPrecinctList = d.getBorderPrecinctList();
			District[] neighborDistrictList = d.getNeighborDistricts();
			for(District to:neighborDistrictList){
				tryMove(borderPrecinctList,d,to);
			}
		}
		
		Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state.getStateId(), state, "test");
		model.addAttribute("plan", plan);
		return "index";
	}

	private void tryMove(Precinct[] borderPrecinctList,District from, District to) {
		double originalScore=calculateGoodness(from)+calculateGoodness(to);
		for (Precinct precinct : borderPrecinctList) {
			if(checkConstraint(precinct,to))
				moveTo(precinct, from, to);
			else
				continue;
			double newScore=calculateGoodness(from)+calculateGoodness(to);
			if ( newScore> originalScore) 
				originalScore=newScore;
			else
				moveTo(precinct,to,from);
		}
		
	}
	
	public double calculateGoodness(District d) {
		double compactness = calculateCompactness(d, weights[0]);
		double population = calculatePopulation(d, weights[1]);
		double politicalFairness = calculatePoliticalFairness(d, weights[2]);
		double contiguity = calculateContiguity(d, weights[3]);
		double racialFairness = calculateRacialFairness(d, weights[4]);
		double totalGoodness = compactness + population + politicalFairness + contiguity + racialFairness;
		return totalGoodness;
	}

	public boolean checkConstraint(Precinct precinct, District d2) {
		District[] neighborDistrictList = d2.getNeighborDistricts();
		for (District d : neighborDistrictList) {
			if (Arrays.asList(d.getBorderPrecinctList()).contains(precinct)) {
				return true;
			}
		}
		return false;
	}

	public void moveTo(Precinct precinct, District d1, District d2) {
		int[] inPList = d2.getIntoPList();
		int[] newPList = new int[inPList.length + 1];
		for (int i = 0; i < inPList.length; i++)
			newPList[i] = inPList[i];
		newPList[newPList.length - 1] = precinctId;
		d2.setIntoPList(newPList);
	}

	public double calculateCompactness(District d, double weight) {
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
