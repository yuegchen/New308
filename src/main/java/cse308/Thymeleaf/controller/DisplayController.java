package cse308.Thymeleaf.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cse308.Thymeleaf.message.StateComparisonMessage;
import cse308.Thymeleaf.model.State;
import cse308.Thymeleaf.model.StateComparison;

@RestController
@RequestMapping("/display")
public class DisplayController {
	
	@RequestMapping(value = "/state-comparison", method = RequestMethod.POST)
	public StateComparisonMessage getStateComparison(StateComparison stateComparison){
		StateComparisonMessage stateComMessage = new StateComparisonMessage(new State(stateComparison.getStateId1()), 
				new State(stateComparison.getStateId2()));
		return stateComMessage;
	}
}
