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

	@MessageMapping("/redistrict")
	@SendTo("/redistrict/reply")
	public String processRequestsFromClient(@Payload String request){
		String requestType = new Gson().fromJson(request, Map.class).get("request").toString().toUpperCase();
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
		
		@Override
		public void run(){
			while(endingCondition){
				try {
					Thread.sleep(3000);
					
					smt.convertAndSend("/redistrict/reply", "Keep Going");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("done");
		}
	}
}
