package cse308.Thymeleaf.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

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
		RedistrictHelpers rh = new RedistrictHelpers();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager em = emf.createEntityManager();		
		@Override
		public void run(){
			while(endingCondition){
				try {
					List<District> districts = rh.getDistrictsByState(stateId, em);

					end_redistricting:
					if(endingCondition){
						for(District district:districts){
							List<Precinct> borderPrecincts = district.initBorderingPrecinctList();
							List<District> neighborDistricts = district.getNeighborDistricts();

							for(District to : neighborDistricts){
								while(isPaused){
									Thread.sleep(1000);
								}
								if(endingCondition){
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

		private void tryMove(List<Precinct> borderPrecincts, District fromDistrict, District toDistrict) throws IOException {
			double originalScore = rh.calculateGoodness(fromDistrict, weights) + rh.calculateGoodness(toDistrict, weights);
			List<Precinct> tempBorderPList = new ArrayList<Precinct>();
			for(Precinct precinct : borderPrecincts){
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
				double newScore = rh.calculateGoodness(fromDistrict, weights) + rh.calculateGoodness(toDistrict, weights);
				if (newScore > originalScore) {
					originalScore = newScore;
					non_steps = 0;
				}
				else{
					non_steps++;
					smt.convertAndSend("/redistrict/reply", new JsonParser()
						.parse(rh.moveTo(precinct, toDistrict, fromDistrict, false))
						.getAsJsonObject());
				}
			}
		}
	}
}
