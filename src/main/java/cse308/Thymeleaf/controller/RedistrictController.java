package cse308.Thymeleaf.controller;

import java.util.Map;

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

import cse308.Thymeleaf.RecoloringOption;

@Controller
public class RedistrictController {

	@Autowired
	private AsyncTaskExecutor te;
	
    @Autowired
    private SimpMessagingTemplate smt;
    
    private boolean endingCondition = true;
    private int     stateId;

	@MessageMapping("/redistrict")
	@SendTo("/redistrict/reply")
	public String processRequestsfromClient(@Payload String request){
		Map request = new Gson().fromJson(request, Map.class);
		String requestType = request.get("request").toString().toUpperCase();
		switch(RecoloringOption.valueOf(requestType)){
			case START:
				endingCondition = true;
				this.te.execute(new RedistrictingThread());
				break;
			case PAUSE:
				break;
			case RESUME:
				break;
			case STOP:
				endingCondition = false;
				break;
			default:
				break;
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
							List<Precinct> borderPrecincts = district.initBorderingPrecincts();
							List<District> neighborDistricts = district.getNeighborDistricts();
							for(District to:neighborDistricts){
								tryMove(borderPrecincts,district,to);
								if(endingCondition){
									break end_redistricting;
								}
							}

						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void tryMove(List<Precinct> borderPrecincts, District fromDistrict, District toDistrict) throws IOException {
			double originalScore = rh.calculateGoodness(fromDistrict) + rh.calculateGoodness(toDistrict);
			for (Precinct precinct : borderPrecincts) {
				if(rh.checkConstraint(precinct,toDistrict))
					rh.moveTo(precinct, fromDistrict, toDistrict, false);
				else
					continue;
				double newScore = rh.calculateGoodness(fromDistrict) + rh.calculateGoodness(toDistrict);
				if (newScore > originalScore) {
					originalScore = newScore;
					Thread.sleep(1000);
				}
				else{
					rh.moveTo(precinct,toDistrict,fromDistrict, true);

				}
			}
		}
	}
}
