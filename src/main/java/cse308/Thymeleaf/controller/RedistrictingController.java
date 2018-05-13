package cse308.Thymeleaf.controller;

import java.io.IOException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import cse308.Thymeleaf.form.RedistrictingForm;
import cse308.Thymeleaf.form.RegisterForm;
import cse308.Thymeleaf.model.*;


@Controller
public class RedistrictingController {
	private static State state;
	private static double[] weights={1,0,0,0,0};
	private static int steps=0;
	private static int non_steps=0;
	private static int MAX_MOVES=0;
	private static int MAX_NON_IMPROVED_STEPS=0;
	private static String move="";

	@RequestMapping(value = { "/redistrict" }, method = RequestMethod.POST)
	public String startAlgo(Model model, //
			@ModelAttribute("redistrictingForm") RedistrictingForm redistrictingForm) throws IOException {
		System.err.println("Enter startAlgo!!!");

		File file = new File("C:\\eclipse\\Projects\\New308\\src\\main\\resources\\static\\externalProperty\\Property.txt");

		BufferedReader br = new BufferedReader(new FileReader(file));

		String st;
		int i=0;
		while ((st = br.readLine()) != null){
			System.out.println(st);
			int index=st.indexOf('=');
			if(i==0){
				MAX_MOVES=Integer.parseInt(st.substring(index+1, st.length()));
			}
			else{
				MAX_NON_IMPROVED_STEPS=Integer.parseInt(st.substring(index+1, st.length()));
			}
			i++;
		}
		System.out.println(MAX_MOVES);
		System.out.println(MAX_NON_IMPROVED_STEPS);

		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		state = entitymanager.find(State.class, 27);
		
		List<District> dList = state.initDistList();
		
		for(District d:dList){
			List<Precinct> borderPrecinctList = d.initBorderingPrecinctList();
			List<District> neighborDistrictList = d.getNeighborDistricts();
			
			for(District to:neighborDistrictList){
				System.out.println("First district: " + d.getDId() + " Neighbor District: " + to.getDId());
				
				System.out.println("stop");
				tryMove(borderPrecinctList,d,to);
				if(steps>=MAX_MOVES){
					System.err.println("steps exceeds MAX_MOVES");
					Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state, "test");
					model.addAttribute("plan", plan);
					return "index";
				}
				if(non_steps>=MAX_NON_IMPROVED_STEPS){
					System.err.println("non-steps exceeds MAX_NON_IMPROVED_STEPS");
					Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state, "test");
					model.addAttribute("plan", plan);
					return "index";
				}
			}
		}
		
		System.out.println("stop");
		Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state, "test");
		model.addAttribute("plan", plan);
		return "index";
	}

	private void tryMove(List<Precinct> borderPrecinctList,District from, District to) throws IOException {
		System.err.println("Enter tryMove!!!");
		double originalScore=calculateGoodness(from)+calculateGoodness(to);
		System.out.println("originalScore: "+originalScore);
		System.err.println("stop4");
		System.out.println("Border Precinct List Size " + borderPrecinctList.size());
		List<Precinct> tempBorderPList=new ArrayList<Precinct>();
		for(Precinct precinct : borderPrecinctList){
			tempBorderPList.add(precinct);
		}
		for (Precinct precinct : tempBorderPList) {
			if(checkConstraint(precinct,to)){
				moveTo(precinct, from, to, false);
				steps++;
				System.err.println("stop2");
			}
			else
				continue;
			double newScore=calculateGoodness(from)+calculateGoodness(to);
			System.out.println("new Score: "+newScore);
			if (newScore > originalScore) {
				originalScore=newScore;
				non_steps=0;
			}
			else{
				non_steps++;
				moveTo(precinct,to,from, true);
			}
		}
		
		System.err.println("stop3");
		
	}
	
	public double calculateGoodness(District d) throws IOException {
		System.err.println("Enter calculateGoodness!!!");
		double compactness = calculateCompactness(d, weights[0]);
		double population = calculatePopulation(d, weights[1]);
		double politicalFairness = calculatePoliticalFairness(d, weights[2]);
		double contiguity = calculateContiguity(d, weights[3]);
		double racialFairness = calculateRacialFairness(d, weights[4]);
		double totalGoodness = compactness + population + politicalFairness + contiguity + racialFairness;
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

	public void moveTo(Precinct precinct, District d1, District d2, boolean out) {
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
		move = "{ \"movedPrecincts\" : [[ 'districtId': '"+d2.getDId()+"', \"precinctId\": ["+precinct.getPid()+"]]]}";
		System.out.println("move "+precinct.getPid()+" from "+d1.getDId()+" to "+d2.getDId());
	}
	
	public double calculateCompactness(District d, double weight) throws IOException {
		System.err.println("Enter calculateCompactness!!!");
		System.err.println("try get Perimeter!!!");
		double perimeter = d.getPerimeter();
		System.err.println("I get Perimeter!!!");
		System.err.println("try get Area!!!");
		double area = d.getArea();
		System.err.println("I get Area!!!");
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
