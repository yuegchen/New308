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
	private static double[] weights={1,0,0};
	private static int steps=0;
	private static int non_steps=0;
	private static int MAX_MOVES=0;
	private static int MAX_NON_IMPROVED_STEPS=0;
	private static int objectiveScore=0;
	private static String move="";
	private static String score="";
	private static Plan plan;

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
		//web statistics
		entitymanager.getTransaction().begin(); 
		Statistics stat = entitymanager.find(Statistics.class, 1);
		if(state.getStateId()==9)
			stat.setConnecticut(stat.getConnecticut()+1);
		else if(state.getStateId()==25)
			stat.setMassachusetts(stat.getMassachusetts()+1);
		else
			stat.setMinnesota(stat.getMinnesota()+1);;
		entitymanager.getTransaction().commit();
		
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
		String intoList="{ \"movedPrecincts\" : [";
		for(District d:dList){
			intoList=intoList+"[ 'districtId': '"+d.getDId()+"', \"precinctId\": [";
			for(Integer pid:d.getIntoPList()){
				intoList=intoList+pid+",";
			}
			intoList=intoList.substring(0, intoList.length()-1);
			intoList=intoList+"]]";
		}
		intoList=intoList+"]}";
		plan.setStateName(intoList);
		System.out.println(intoList);
		
		System.out.println("stop");
		Plan plan = new Plan(1, new Date().toString(), state.getStateName(), state, "test");
		model.addAttribute("plan", plan);
		return "index";
	}

	private void tryMove(List<Precinct> borderPrecinctList,District from, District to) throws IOException {
//		System.err.println("Enter tryMove!!!");
		
		double originalScore=calculateGoodness(from,to);
		double goal=originalScore*1.37;
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
				
				move = "{ \"movedPrecincts\" : {\"districtId\": "+to.getDId()+", \"precinctId\": "+precinct.getPid()+"} }";
				steps++;
				System.out.println("step"+steps+" :move "+precinct.getPid()+" from "+from.getDId()+" to "+to.getDId());
				System.err.println("stop2");
			}
			else
				continue;
			double newScore=calculateGoodness(from,to);
			System.out.println("new Score: "+newScore);
			if (newScore > originalScore) {
				originalScore=newScore;
				score="{ \"score\" : {\"objective\": "+newScore/goal*2+""+"} }";
				System.out.println("objective Score: "+newScore/goal*2);
				non_steps=0;
			}
			else{
				non_steps++;
				moveTo(precinct,to,from, true);
				move = "{ \"movedPrecincts\" : {\"districtId\": "+to.getDId()+", \"precinctId\": "+precinct.getPid()+"} }";
			}
		}
		
		System.err.println("stop3");
		
	}
	public double calculateGoodness(District d,District d2) throws IOException {
//		System.err.println("Enter calculateGoodness!!!");
		double compactness1 = calculateCompactness(d, weights[0]);
		double compactness2 = calculateCompactness(d2, weights[0]);
		
		double population = calculatePopulation(d,d2, weights[1]);
		double politicalFairness1 = calculatePoliticalFairness(d, weights[2]);
		double politicalFairness2 = calculatePoliticalFairness(d2, weights[2]);
		double totalGoodness = compactness1+compactness2 + population*2 + politicalFairness1+politicalFairness2;
		return totalGoodness; 
	}

	public boolean checkConstraint(Precinct precinct, District d2) {
		List<Precinct> neighborPrecinctList = precinct.getNeighborPrecinctList();
		System.err.println("neighborPrecinctList Size" + neighborPrecinctList.size());
		System.err.println("Precinct id: " + precinct.getPid());
		boolean vioContiguity=true;
		int oid=precinct.getCd();
		precinct.setCd(d2.getDId());
		for (Precinct p : neighborPrecinctList) {
			List<Precinct> tempNeighborPrecinctList = p.getNeighborPrecinctList();
			for(Precinct p2:tempNeighborPrecinctList){
				if(p2.getCd()==p.getCd()){
					vioContiguity=false;
				}
			}
			if(vioContiguity){
				precinct.setCd(oid);
				return false;
			}
			if (p != null && p.getCd()==d2.getDId()) {
				System.out.println("true");
				precinct.setCd(oid);
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
		
		System.out.println("move "+precinct.getPid()+" from "+d1.getDId()+" to "+d2.getDId());
	}
	
	public double calculateCompactness(District d, double weight) throws IOException {
		double perimeter = d.getPerimeter();
		System.err.println("Perimeter: "+perimeter);
		if(perimeter==0){
			return 0;
		}
		double area = d.getArea();
		System.err.println("Area: "+area);
		double r = Math.sqrt(area / Math.PI);
		double equalAreaPerimeter = 2 * Math.PI * r;
		double score = 1 / (perimeter / equalAreaPerimeter);
		System.err.println("score: "+score*weight);
		return score*weight;
	}

	public double calculatePopulation(District d1,District d2, double weight) {
		double popFairness=0;
		double firstPop=d1.getPop();
		double secondPop=d2.getPop();
		System.err.println("firstPop: "+firstPop);
		System.err.println("secondPop: "+secondPop);
		popFairness=1-Math.abs(firstPop-secondPop)/(firstPop+secondPop);
		System.err.println("popFairness: "+popFairness*weight);
		return popFairness*weight;
	}

	public double calculatePoliticalFairness(District d1, double weight) {
		double efficiencyGap=0;
		double politicalFairness=0;
		double dem=d1.getDem();
		double rep=d1.getRep();
	
		double demWaste=0;
		double repWaste=0;
		
		if(dem>rep){
			demWaste+=(dem-0.5);
			repWaste+=rep;
		}
		else{
			demWaste+=dem;
			repWaste+=(rep-0.5);
		}
	
		System.err.println("demWaste: "+demWaste);
		System.err.println("repWaste: "+repWaste);
		efficiencyGap=Math.abs(demWaste-repWaste);
		politicalFairness=1-efficiencyGap;
		System.err.println("politicalFairness: "+politicalFairness*weight);
		return politicalFairness*weight;
	}

	
}
