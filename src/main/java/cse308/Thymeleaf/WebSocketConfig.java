package cse308.Thymeleaf;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer{
	
	public void configureMessageBroker(MessageBrokerRegistry config){
		config.enableSimpleBroker("/redistrict/");
		config.setApplicationDestinationPrefixes("/app");
	}
	
	public void registerStompEndpoints(StompEndpointRegistry registry){
		registry.addEndpoint("/redistrict").withSockJS();
	}
}
