package cse308.Thymeleaf.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Autowired;
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
			stateId = Integer.parseInt(requestJson.get("stateId").toString());
			weights = gson.fromJson(requestJson.get("weights").toString(), double[].class);
			switch(RecoloringOption.valueOf(requestType)){
				case START:
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

		@Override
		public void run(){
			while(endingCondition){
				try {
					ExternalProperties ep = new ExternalProperties();
					int maxMoves = ep.getMaxMoves();
					int maxNonImprovedSteps = ep.getNonImprovedSteps();

					List<District> districts = rh.getDistrictsByState(stateId, em);

					end_redistricting:
					if(steps < maxMoves && nonSteps < maxNonImprovedSteps){
						for(District district:districts){
							List<Precinct> borderPrecincts = district.initBorderingPrecinctList();
							List<District> neighborDistricts = district.getNeighborDistricts();

							for(District to : neighborDistricts){
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
								tryMove(borderPrecincts,district,to);
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

		private void tryMove(List<Precinct> borderPrecinctList,District fromDistrict, District toDistrict) throws IOException {
			double originalScore=rh.calculateGoodness(fromDistrict, toDistrict, weights);
			System.out.println("originalScore: "+originalScore);
			List<Precinct> tempBorderPList=new ArrayList<Precinct>();
			for(Precinct precinct : borderPrecinctList){
				tempBorderPList.add(precinct);
			}
			for (Precinct precinct : tempBorderPList) {
				if(rh.checkConstraint(precinct,toDistrict)){
					smt.convertAndSend("/redistrict/reply", new JsonParser()
							.parse(rh.moveTo(precinct, fromDistrict, toDistrict, false))
							.getAsJsonObject());
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
					smt.convertAndSend("/redistrict/reply", new JsonParser()
						.parse(rh.moveTo(precinct, toDistrict, fromDistrict, false))
						.getAsJsonObject());
				}
			}
		}
	}
}
