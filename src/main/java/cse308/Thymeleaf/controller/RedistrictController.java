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

@Controller
public class RedistrictController {

	@Autowired
	private AsyncTaskExecutor te;
	
    @Autowired
    private SimpMessagingTemplate smt;
    
    private boolean endingCondition = true;
    private boolean isPaused = false;
    private int     stateId;
    private double [] weights;

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
					weights = gson.fromJson(requestJson.get("weights").toString(), double[].class);
					endingCondition = true;
					this.te.execute(new RedistrictingThread());
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
			while(endingCondition){
				try {
					ExternalProperties ep = new ExternalProperties();
					int maxMoves = ep.getMaxMoves();
					int maxNonImprovedSteps = ep.getNonImprovedSteps();

					List<District> districts = rh.getDistrictsByState(stateId, em);
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
					end_redistricting:
					if(steps < maxMoves && nonSteps < maxNonImprovedSteps){
						for(Map<District, District> map : neighborDistrictPairs){	
							while(isPaused){
								Thread.sleep(1000);
							}
							if(steps >= maxMoves){
								System.err.println("steps exceeds MAX_MOVES");
								break end_redistricting;
							}
							if(nonSteps >= maxNonImprovedSteps){
								System.err.println("non-steps exceeds MAX_NON_IMPROVED_STEPS");
								break end_redistricting;
							}
							for(Map.Entry<District, District> entry: map.entrySet())
								tryMove(borderPrecinctsArray, entry.getKey(), entry.getValue());
							//Thread.sleep(2000);
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

		private void tryMove(List<Precinct>[] borderPrecinctsArray,District fromDistrict, District toDistrict) throws IOException {
			double originalScore=rh.calculateGoodness(fromDistrict, toDistrict, weights);
			System.out.println("originalScore: "+originalScore);
			Map<Integer, Integer> movedPrecincts = new HashMap<Integer, Integer>();
			List<Precinct> fromBorderPrecincts = borderPrecinctsArray[fromDistrict.getDId()-1];
			List<Precinct> toBorderPrecincts = borderPrecinctsArray[toDistrict.getDId()-1];
			List<Precinct> tempBorderPList=new ArrayList<Precinct>();
			for(Precinct precinct : fromBorderPrecincts){
				for(Precinct precinct2: toBorderPrecincts){
				if(precinct.isNeighbor(precinct2.getPid()))
					tempBorderPList.add(precinct);
				}
			}
			for (Precinct precinct : tempBorderPList) {
				if(rh.checkConstraint(precinct,toDistrict, movedPrecincts)){
					smt.convertAndSend("/redistrict/reply", rh.moveTo(precinct, fromDistrict, toDistrict, false));
					movedPrecincts.put(precinct.getPid(), toDistrict.getDId());
					toBorderPrecincts.add(precinct);
					fromBorderPrecincts.remove(precinct);
					steps++;
				} 
				else
					continue;
				double newScore=rh.calculateGoodness(fromDistrict,toDistrict, weights);
				System.out.println("new Score: "+newScore);
				if (newScore > originalScore) {
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
			}
			
		}
	}
}
