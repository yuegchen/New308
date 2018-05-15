package cse308.Thymeleaf;

import java.io.IOException;
import java.util.List; 
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import cse308.Thymeleaf.model.District;
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
		double politicalFairness2 = calculatePoliticalFairness(d2, weights[2]);
		double totalGoodness = compactness1+compactness2 + population*2 + politicalFairness1+politicalFairness2;
		return totalGoodness; 
	}

	public boolean checkConstraint(Precinct precinct, District d2, Map<Integer, Integer> movedPrecincts) {
		List<Precinct> neighborPrecinctList = precinct.getNeighborPrecinctList();
		for (Precinct p : neighborPrecinctList) {
			if(movedPrecincts.containsKey(p.getPid())){
				p.setCd(movedPrecincts.get(p.getPid()));
			}
			if (p != null && p.getCd()==d2.getDId()) {
				return true;
			}
			List<Precinct> tempNeighborPrecinctList = p.getNeighborPrecinctList();
			for(Precinct p2:tempNeighborPrecinctList){
				if(movedPrecincts.containsKey(p2.getPid())){
					p2.setCd(movedPrecincts.get(p2.getPid()));
				}
				if(p2.getCd()==p.getCd()){
					return true;
				}
			}
		}
		return false;
	}

	public String moveTo(Precinct precinct, District d1, District d2, boolean out) {
		List<Precinct> precinctList = d1.initPrecList();
		System.out.println("size before remove: "+precinctList.size());
		for(Precinct p:precinctList){
			if(p.getPid()==precinct.getPid()){
				
				precinctList.remove(p);
				break;
			}
		}
		System.out.println("size after remove: "+precinctList.size());
//		precinctList.remove(precinct);
		d1.setPrecinctList(precinctList); 
		
		List<Precinct> borderingPrecinctList = d1.initBorderingPrecinctList();
		for(Precinct p:borderingPrecinctList){
			if(p.getPid()==precinct.getPid()){
				borderingPrecinctList.remove(p);
				break;
			}
		}
//		borderingPrecinctList.remove(precinct);
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
		return "{ \"movedPrecincts\" : {\"districtId\": "+d2.getDId()+", \"precinctId\": "+precinct.getPid()+"} }";
		
		
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
//		System.err.println("firstPop: "+firstPop);
//		System.err.println("secondPop: "+secondPop);
		popFairness=1-Math.abs(firstPop-secondPop)/(firstPop+secondPop);
//		System.err.println("popFairness: "+popFairness*weight);
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
		efficiencyGap=Math.abs(demWaste-repWaste);
		politicalFairness=1-efficiencyGap;
		return politicalFairness*weight;
	}

	
}
