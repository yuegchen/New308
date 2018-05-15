package cse308.Thymeleaf.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import cse308.Thymeleaf.ExternalProperties;
import cse308.Thymeleaf.RecoloringOption;
import cse308.Thymeleaf.RedistrictHelpers;
import cse308.Thymeleaf.model.District;
import cse308.Thymeleaf.model.Precinct;
import cse308.Thymeleaf.model.State;

@Controller
public class RedistrictController {

	@Autowired
	private AsyncTaskExecutor te;
	
    @Autowired
    private SimpMessagingTemplate smt;
    
    private boolean endingCondition = true;
    private boolean contiguity = true;
    private boolean isPaused = false;
    private boolean stoppingCondition = false;
    private int     stateId;
    private double [] weights;
//    private State originalState;
//    private State newState;

	@MessageMapping("/redistrict")
	@SendTo("/redistrict/reply")
	public String processRequestsfromClient(@Payload String request){
		Gson gson = new Gson();
		Map requestJson = gson.fromJson(request, Map.class);
		try{
			String requestType = requestJson.get("request").toString().toUpperCase();
			switch(RecoloringOption.valueOf(requestType)){
				case START:
					stateId = (int) Double.parseDouble(requestJson.get("stateId").toString());
//					originalState=new State(stateId);
//					newState=new State(stateId);
					System.out.println("stateId: "+stateId);
					contiguity = (boolean) requestJson.get("contiguity");
					System.out.println("contiguity: "+contiguity);
					weights = gson.fromJson(requestJson.get("weights").toString(), double[].class);
					double total=0;
					for(double w:weights){
						total+=w;
					}
					for(int i=0;i<weights.length;i++){
						weights[i]=weights[i]/total;
						System.out.println("weight "+i+" : "+weights[i]);
					}
					endingCondition = true;
					while(true){
						this.te.execute(new RedistrictingThread());
						while(!stoppingCondition){
							Thread.sleep(5000);
						}
						endingCondition = true;
						break;
					}
					break;
				case PAUSE:
					isPaused = true;
					break;
				case RESUME:
					isPaused = false;
					break;
				case STOP:
					isPaused = false;
					endingCondition = false;
					break;
				case DISCONNECT:
					stoppingCondition = true;
					break;
				case COMPARE:
					smt.convertAndSend("/redistrict/reply" );
				default:
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return "ok";
	}
	
	@MessageExceptionHandler
	@SendTo("/redistrict/errors")
	public String handleException(Throwable exception){
		return exception.getMessage();
	}
	
	@Component
	@Scope("prototype")
	public class RedistrictingThread implements Runnable{
		private RedistrictHelpers rh = new RedistrictHelpers();
		private EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		private EntityManager em = emf.createEntityManager();	
		private int	steps = 0;
		private int nonSteps = 0;

		@SuppressWarnings("unchecked")
		@Override
		public void run(){
			end_redistricting:
			if(endingCondition){
				try {
					ExternalProperties ep = new ExternalProperties();
					int maxMoves = ep.getMaxMoves();
					int maxNonImprovedSteps = ep.getNonImprovedSteps();

					List<District> districts = rh.getDistrictsByState(stateId, em);
//					for(District d:originalState.initDistList()){
//						d.setCompactness(rh.calculateCompactness(d, 1));
//						d.setPopulation(d.getPop());
//						d.setEfficiencyGap(1-rh.calculatePoliticalFairness(d, 1));
//					}
//					newState.setDistList(districts);
					List<Precinct>[] borderPrecinctsArray = (List<Precinct>[])new List[districts.size()];
					for(int i = 0; i < districts.size(); i++){
						borderPrecinctsArray[districts.get(i).getDId()-1] = districts.get(i).initBorderingPrecinctList();
					}
					List<Map<District, District>> neighborDistrictPairs = new ArrayList<Map<District, District>>();
					for(District fromDistrict : districts){
						for(District toDistrict : districts){
							System.out.println("fromDistrictId: " + fromDistrict.getDId() + " ToDistrictId: " + toDistrict.getDId());
							System.out.println("fromDistrict: " + fromDistrict + " ToDistrict: " + toDistrict);
							System.out.println("checked"); 
							if(fromDistrict.isNeighbor(toDistrict.getDId())){
								Map<District, District> neighborDistrictMapping = new HashMap<District, District>();
								neighborDistrictMapping.put(fromDistrict, toDistrict);
								neighborDistrictPairs.add(neighborDistrictMapping);
							}
						}
					}
					if(steps < maxMoves && nonSteps < maxNonImprovedSteps){
						for(Map<District, District> map : neighborDistrictPairs){	
							if(steps >= maxMoves){
								System.err.println("steps exceeds MAX_MOVES");
								endingCondition = false;
								break end_redistricting;
							}
							if(nonSteps >= maxNonImprovedSteps){
								System.err.println("non-steps exceeds MAX_NON_IMPROVED_STEPS");
								endingCondition = false;
								break end_redistricting;
							}
							for(Map.Entry<District, District> entry: map.entrySet()){
								tryMove(borderPrecinctsArray, entry.getKey(), entry.getValue());
								if(!endingCondition)
									break end_redistricting;
							}
						}
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void tryMove(List<Precinct>[] borderPrecinctsArray,District fromDistrict, District toDistrict) throws IOException, InterruptedException {
			double originalScore=rh.calculateGoodness(fromDistrict, toDistrict, weights);
			System.out.println("originalScore: "+originalScore);
			Map<Integer, Integer> movedPrecincts = new HashMap<Integer, Integer>();
			List<Precinct> fromBorderPrecincts = borderPrecinctsArray[fromDistrict.getDId()-1];
			List<Precinct> toBorderPrecincts = borderPrecinctsArray[toDistrict.getDId()-1];
			List<Precinct> tempBorderPList=new ArrayList<Precinct>();
			for(Precinct precinct : fromBorderPrecincts){
				for(Precinct precinct2: toBorderPrecincts){
				if(precinct.isNeighbor(precinct2.getPid()))
					if(!tempBorderPList.contains(precinct))
						tempBorderPList.add(precinct);
				}
			}
			for (Precinct precinct : tempBorderPList) {
				if(!contiguity||rh.checkConstraint(precinct,toDistrict, movedPrecincts)){
					System.out.println("precinct: " + precinct); 
					smt.convertAndSend("/redistrict/reply", rh.moveTo(precinct, fromDistrict, toDistrict, false));
					movedPrecincts.put(precinct.getPid(), toDistrict.getDId());
					toBorderPrecincts.add(precinct);
					fromBorderPrecincts.remove(precinct);
					steps++;
					Thread.sleep(1000);
					while(isPaused){
						Thread.sleep(1000);
					}
					if(!endingCondition)
						return;
				} 
				else
					continue;
				double newScore=rh.calculateGoodness(fromDistrict,toDistrict, weights);
				
				System.out.println("new Score: "+newScore);
				if (newScore > originalScore) {
					fromDistrict.setCompactness(rh.calculateCompactness(fromDistrict, 1));
					toDistrict.setCompactness(rh.calculateCompactness(toDistrict, 1));
					fromDistrict.setEfficiencyGap(1-rh.calculatePoliticalFairness(fromDistrict, 1));
					toDistrict.setCompactness(1-rh.calculatePoliticalFairness(toDistrict, 1));
					originalScore=newScore;
					nonSteps=0;
				}
				else{
					nonSteps++;
					smt.convertAndSend("/redistrict/reply", rh.moveTo(precinct, toDistrict, fromDistrict, true)); 
					movedPrecincts.put(precinct.getPid(), fromDistrict.getDId());
					fromBorderPrecincts.add(precinct);
					toBorderPrecincts.remove(precinct);
				}
				while(isPaused){
					Thread.sleep(1000);
				}
				if(!endingCondition)
					return;
			}
		}
	}
}
